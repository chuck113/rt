package com.rt.rhyme

import collection.mutable.{ListBuffer, Map => MutableMap}
import java.lang.String
import com.rt.Properties
import com.rt.rhyme.StringRhymeUtils._
import collection.immutable.Map


class RhymeList {
  private var rhymes: List[Rhyme] = List[Rhyme]()

  /**returns any rhyme objects whos parts are a subset of the parts in the given rhyme */
  def subsetOf(rhyme: Rhyme): List[Rhyme] = {
    rhymes.filter(_.isSubsetOf(rhyme))
  }

  /**returns any rhyme objects whos parts are a superset of the parts in the given rhyme */
  def superSetOf(rhyme: Rhyme): List[Rhyme] = {
    rhymes.filter(_.isSupersetOf(rhyme))
  }

  def addRhymes(rhymesToAdd: List[Rhyme]) = {
    rhymesToAdd.foreach(addRhyme)
  }

  def addRhyme(rhyme: Rhyme) = {
    // if a supersets of the rhyme exists, don't add it
    // is a subsets of the rhyme exists, remove them all and add it
    if (superSetOf(rhyme).size == 0) {
      //println("BEFORE: "+rhymes)
      //println("sub set is : "+subsetOf(rhyme))
      rhymes = rhymes -- subsetOf(rhyme)
      addInternal(rhyme)
      //println("AFTER: "+rhymes)
    }
  }

  def addInternal(rhyme: Rhyme) = {
    if (!rhymes.contains(rhyme)) {
      rhymes = rhyme :: rhymes
    }
    //println("rhymes are now: "+rhymes)
  }

  def allRhymes(): List[Rhyme] = {
    rhymes
  }
}

case class Rhyme(parts: List[String], lines: List[String]) {
  def addLine(line: String): Unit = {
    if (!lines.contains(line)) {
      line :: lines
    }
  }

  def rating(): Int = {
    parts.foldLeft(1) {(n, st) => {n * st.length}}
  }

  /**
   * Checks if this rhyme is a subset of rhyme 'that'
   *
   * (SPARK, BARK) is a subset of (SPARK, BARK, PARK, DARK) as all of its values are
   * present. entries in operand must appear in parts.
   */
  def isSubsetOf(that: Rhyme): Boolean = {
    //println("is "+parts+" a subset of "+that.parts+": " + (parts.forall(that.parts.contains))+", "+(containsOneValueInCommon(that.parts, parts)))
    parts.forall(that.parts.contains) && containsOneValueInCommon(that.parts, parts)
    // all of my parts must apper in that's parts
  }

  def isSupersetOf(that: Rhyme): Boolean = {
    //println("is "+parts+" a super set of "+that.parts+": " + (that.parts.forall(this.parts.contains))+", "+(containsOneValueInCommon(that.parts, parts)))
    that.parts.forall(this.parts.contains) && containsOneValueInCommon(that.parts, parts)
  }

  private def containsOneValueInCommon(l1: List[String], l2: List[String]): Boolean = {
    l1.intersect(l2).size > 0
  }

  def addPart(part: String, line: String): Rhyme = {
    if (!lines.contains(line)) {
      if (!parts.contains(part))
        Rhyme(part :: parts, line :: lines)
      else
        this
    } else {
      if (!parts.contains(part))
        Rhyme(part :: parts, lines)
      else
        this
    }
  }
}

/**
 * Finds rhymes in a given list of sentences/lines.
 */
class RhymeFinder(val rhymeMap: RhymeMap) {

  /**
   * Should return a list of objects, where the object is the lines that participate in the ryhyme and the
   * found rhyme parts.
   *
   * Assume individualWords on the current line that rhyme with individualWords 2 lines earlier with rhyme with a word in the
   * intermediate line
   *
   */
//  def findRhymesInLines2(lines: List[String]): List[Rhyme] = {
//    val foundRhymes: RhymeList = new RhymeList();
//    var lastLineSearched = ""
//
//    for (line <- lines) {
//      val rhymes: List[Rhyme] = findUniqueRhymes(List(lastLineSearched, line))
//      foundRhymes.addRhymes(rhymes)
//      lastLineSearched = line
//    }
//    foundRhymes.allRhymes
//  }

//  private def findRhymePairs(lines: List[String]): List[(String, String)] = {
//    val validStrings: List[String] = collectUnEmptyString(lines)
//    val rhymePairsInLine: List[(String, String)] = findRhymingWordsMap(validStrings).keys.toList;
//    removeUnwantedWords(rhymePairsInLine)
//  }

  private def findRhymePairs(lines: List[String]): Map[(String, String), List[String]] = {
    val validStrings: List[String] = collectUnEmptyString(lines)
    val rhymePairsInLine: Map[(String, String), List[String]] = findRhymingWordsMap(validStrings)
    val wantedWords: Map[(String, String), List[String]] = removeUnwantedWords2(rhymePairsInLine)
    removeNonConsecutiveLineRhymes(lines, wantedWords)
  }

  /**
   * Removes found rhymes that happen in lines that are not next to each other. This
   * stops non-rhymes where two words in the line buffer happen to rhyme
   */
  private def removeNonConsecutiveLineRhymes(lines: List[String], rhymes: Map[(String, String), List[String]]): Map[(String, String), List[String]] = {
    val linesReversed = lines.reverse

    rhymes.filter(entry => {
      val entryLines: List[String] = entry._2
      if (entryLines.size == 2) {
        val firstIndex: Int = lines.indexOf(entryLines(0))
        val secondIndex: Int = lines.indexOf(entryLines(1))
        firstIndex - 1 == secondIndex || secondIndex - 1 == firstIndex
      } else {
        true
      }
    })
  }

  def newRhymes(inPlay: List[(String, String)], found: List[(String, String)]): List[(String, String)] = {
    found.filter(x => inPlay.contains(x))
  }

  /**
   * Idea is to iterate over lines, finding rhymes in the line. Seen lines are stored in a buffer so that
   * each iteration searches previous lines for rhymes, this means we can look for rhymes that
   * span an arbitrary amount of lines.
   *
   * If we come accross a line that does not have a rhyme pair which corresponds to a rhyme pair
   * in the buffer, we remove those lines from the buffer and produce a rhyme result. The buffer
   * is then made up of the corresponding lines in the buffer.
   */
  def findRhymesInLines(lines: List[String]): List[Rhyme] = {
    // line buffer, stores rhyme pairs mapped on to the line they were found in
    //val buffer:MutableMap[(String, String), List[String]] = new MutableMap[(String, String), List[String]]()

    val foundRhymes: ListBuffer[Rhyme] = new ListBuffer[Rhyme]()
    val linesInPlay: ListBuffer[String] = new ListBuffer[String]()

    val setsInPlay: RhymePartSetHolder = new RhymePartSetHolder(rhymeMap)

    val rhymePairsInPlay: ListBuffer[(String, String)] = new ListBuffer[(String, String)]()

    (lines ::: List("")).foreach(line => { // append one to flush data structures at end
      //lprintln("iterating " + line)
      linesInPlay.append(line)
      //val rhymePairs: List[(String, String)] = findRhymePairs(linesInPlay.toList)
      val rhymePairsMap: Map[(String, String), List[String]] = findRhymePairs(linesInPlay.toList)
      //println("found rhymes: " + rhymePairs2)
      val rhymePairs = rhymePairsMap.keys.toList

      // all rhyme parts in play
      val existingRhymes: List[String] = setsInPlay.allRhymeParts
      //println("parts in play: " + existingRhymes)

      // new rhyme parts found in the last line - add to setsInPlay
      val newRhymesUnFiltered: List[String] = pairsWithPartsNotPresent(rhymePairs, existingRhymes).removeDuplicates

      // TODO remove rhymes that happen over non-consecutive lines

      // remove 'new' rhymes that we have seen before and because they were found in older
      // lines. The parts will exist in foundRhymes, and the line(s) in the Rhyme object
      // won't be the current line. Should be OK to remove any parts that don't appear
      // in the current line and have been rhymed in previous lines
      val newRhymes: List[String] = removeSeenRhymes(newRhymesUnFiltered, line, foundRhymes.toList)
      //println("new Rhymes: " + newRhymes + ", newRhymesUnFiltered: " + newRhymesUnFiltered)

      // rhyme sets to be removed - they will have not appeared in the last line, remove from set
      // what to remove? rhymes in setsInPlay that aren't in newRhymes
      val removedParts: List[List[String]] = setsInPlay.removeEntriesWhichDontMatchAny(newRhymes)

      removedParts.foreach(p => {
        val lines: List[String] = findLinesContainingParts(linesInPlay.toList, p)
        foundRhymes.append(Rhyme(p, lines))
        println("new rhyme: " + Rhyme(p, lines))
      })

      setsInPlay.addParts(newRhymes)

      // updates the buffer - removes lines that do not contain any parts in setsInPlay
      // but NOT the last one because the next line may rhyme with words in it
      removeLinesNotContaingParts(linesInPlay, setsInPlay);
    })
    foundRhymes.toList
  }

  // what if its a new word that is rhymed with a previous line that
  // also appears as part of a previous found line? will be OK because
  // the new word will be in latest line and won't be considered
  private def removeSeenRhymes(newRhymeParts: List[String], latestLine: String, foundRhymes: List[Rhyme]): List[String] = {
    // get all parts in newRhymeParts that dont' appear in latestLine
    val previousParts: List[String] = notContainedIn(latestLine, newRhymeParts)

    // see if there is a rhyme entry for a subset of those parts
    var toRemove = List[String]()

    foundRhymes.foreach(r => {
      val parts: List[String] = r.parts
      // if parts contain all previousParts
      if (parts.forall(x => previousParts.contains(x))) {
        toRemove = parts ::: toRemove
      }
    })
    //println("removeSeenRhymes would remove "+ toRemove)
    newRhymeParts.filter(x => !toRemove.contains(x))
  }

  /**
   *  Returns all of the strings in query that don't appear in line
   */
  private def notContainedIn(line: String, query: List[String]): List[String] = {
    val words: List[String] = individualWords(line)
    query.filter(x => !words.contains(x))
  }


  def pairsToList(pairs: List[(String, String)]): List[String] = {
    pairs.foldLeft(List[String]()) {
      (res, pair) => {
        pair._1 :: res
        pair._2 :: res
      }
    }
  }

  /**
   * Returns a list of strings which are present in pairs and not
   * present in parts
   */
  def pairsWithPartsNotPresent(pairs: List[(String, String)], parts: List[String]): List[String] = {
    pairsToList(pairs).filter(x => !parts.contains(x))
  }

  /**
   * Keeps the lat line in lines, Removes the strings from the lines which don't contain
   * any of the parts conainted in setsInPlay, and returns those lines prepended to the
   * last line
   */
  def removeLinesNotContaingParts(lines: ListBuffer[String], setsInPlay: RhymePartSetHolder): Unit = {
    if (lines.size > 1) {
      //println("remove lines lines: ("+lines.size+") "+lines)
      //println("remove lines all parts: "+setsInPlay.allRhymeParts)
      val lineToKeep: String = lines.toList.last
      lines.remove(lines.size - 1)
      val partsInPlay: List[String] = setsInPlay.allRhymeParts

      val toKeep: List[String] = lines.toList.filter(l => {
        individualWords(l).exists(w => (partsInPlay.contains(w)))
      })

      lines.clear
      toKeep.foreach(line => lines.append(line))
      //println("keeping " + (toKeep.size + 1) + " lines out of " + (lines.size + 1))
      lines.append(lineToKeep)
    }
  }


  private def findLinesContainingParts(lines: List[String], parts: List[String]): List[String] = {
    lines.filter(line => individualWords(line).exists(w => parts.contains(w)))
  }

  // (SPARK, BARK) is a subset of (SPARK, BARK, PARK, DARK) as all of its values are
  // present. entries in operand must appear in parts.
  private def isPartSetASubSetOf(parts: List[String], operand: List[String]): Boolean = {
    operand.forall(parts.contains(_))
  }

  private def collectUnEmptyString(rhymes: List[String]) = {
    rhymes.filter(_.length > 0)
  }

  // returns a list of lines mapped to a list of rhyme parts for the given lines
//  private def findUniqueRhymes(lines: List[String]): List[Rhyme] = {
//    //println("raw rhymes: " + findRhymePairsInLine(lines)+", lines: "+lines)
//
//    val validStrings: List[String] = collectUnEmptyString(lines)
//    val rhymePairsInLine: List[(String, String)] = findRhymePairsInLine(validStrings);
//
//    val rhymes: List[List[String]] = getRhymes(removeUnwantedWords(rhymePairsInLine))
//    //println("findUniqueRhymes rhymes: "+rhymes)
//    buildRhymeObjects(rhymes, lines)
//  }


  private def buildRhymeObjects(sets: List[List[String]], lines: List[String]): List[Rhyme] = {
    sets.foldLeft(List[Rhyme]()) {
      (rhymeList, rhymeParts) => {
        val linesContainingRhymeParts: List[String] = findLinesContainingRhymes(rhymeParts, lines)
        Rhyme(rhymeParts, linesContainingRhymeParts) :: rhymeList
      }
    }
  }

  /**
   * Given a list of rhyme parts, returns the lines in which those parts appear
   */
  private def findLinesContainingRhymes(parts: List[String], lines: List[String]): List[String] = {
    var result: ListBuffer[String] = new ListBuffer[String]()
    parts.foreach(part => {
      lines.foreach(line => {
        val lineUpperCase = line.toUpperCase
        if (lineUpperCase.contains(part) && !result.contains(line)) {
          result.append(line)
        }
      })
    })
    result.toList
  }

  /**
   * Given a list of rhyme part pairs this will returns a list of all the
   * rhyme sets with unique parts in each
   */
  private def getRhymes(pairs: List[(String, String)]): List[List[String]] = {
    val uniqueRhymeParts: List[List[String]] = List[List[String]]()

    val rhymeSet: RhymePartSetHolder = new RhymePartSetHolder(rhymeMap)
    pairs.foreach(pair => {
      rhymeSet.addPart(pair._1)
      rhymeSet.addPart(pair._2)
    })

    rhymeSet.allRhymePartSets
  }

  private def removeUnwantedWords2(rhymePairs: Map[(String, String), List[String]]): Map[(String, String), List[String]] = {
    Map() ++ rhymePairs.filterKeys(p => {
      !Properties.unwantedWords.contains(p._1) && !Properties.unwantedWords.contains(p._2)
    })
  }

  //FIXME only remove words if they're not part of a mutli rhyme
//  private def removeUnwantedWords(rhymePairs: List[(String, String)]): List[(String, String)] = {
//    rhymePairs.filter(p => {
//      !Properties.unwantedWords.contains(p._1) && !Properties.unwantedWords.contains(p._2)
//    })
//  }

//  private def findRhymePairsInLine2(lines: List[String]): Map[String, String] = {
//    print("lines are: ")
//    lines.foreach(println)
//    //val words = individualWords(line)
//    //println("combos: "+buildCombinationPairs(words))
//    //println("words = "+words)
//    var previousWords: ListBuffer[String] = new ListBuffer[String]()
//    var result: MutableMap[String, String] = MutableMap[String, String]()
//
//    lines.foreach(line => {
//      val lineWords: List[String] = individualWords(line)
//      lineWords.foreach(l => previousWords.append(l))
//      println("lineWords: " + lineWords)
//      println("previousWords: " + previousWords)
//      val rhymes: List[(String, String)] = {for{i <- lineWords; j <- previousWords; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j}
//
//      rhymes.foreach(rhyme => {
//        result(rhyme._1) = line
//        result(rhyme._2) = line
//      })
//      println("result is now: " + result)
//    })
//    Map() ++ result
//    //for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j
//  }

  /**
   *  finds the rhymes in one line, returns pairs of words that rhyme
   */
  private def findRhymePairsInLine(line: List[String]): List[(String, String)] = {
    val words = individualWords(line)
    //println("combos: "+buildCombinationPairs(words))
    //println("words = "+words)
    line.foreach(l => {})

    for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j
  }
  //
  //  def makePairs(lines: List[String]):List[(String,String)]={
  //    if(lines.size == 2)List(lines(0) -> lines(1))
  //    else{
  //      val head = lines.head
  //      val tail:List[String] = lines.tail
  //      val pairs = tail.foldLeft(List[(String,String)]()){(list, line)=>{
  //        (head -> line) :: list
  //      }}
  //      pairs ::: makePairs(tail)
  //    }
  //  }

  // what about finding the same rhyme and overwriting it? Will happen if a word rhymes
  // and is present more than once. ideally we'll keep the latest ones, that is the words
  // appearing later in the sequence

  /**
   * If the same word appears in a rhyme
   */
  def findRhymingWordsMap(lines: List[String]): Map[(String, String), List[String]] = {
    findLocationOfPairs(lines, findRhymingWords(lines))

    //    if(lines.size == 1){
    //       null
    //    }else{
    //      println(makePairs(lines))
    //      val pairList: List[(String, String)] = makePairs(lines)
    //
    //      Map() ++ pairList.foldLeft(MutableMap[(String, String), List[List[String]]]()){(map, pair)=>{
    //        val combinedLine = individualWords(pair._1 + pair._2)
    //        val res:List[(String, String)] = for{i <- combinedLine; j <- combinedLine; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j
    //        res.foreach(p => {
    //          if(map.contains(p)){
    //            map(p) = List[String](pair._1 + pair._2) :: map(p)
    //          } else{
    //            map(p) = List(List[String](pair._1 + pair._2))
    //          }
    //          println("map is now "+map)
    //        })
    //        map
    //      }}
    //    }
  }

  def findLocationOfPairs(lines: List[String], foFind: List[(String, String)]): Map[(String, String), List[String]] = {
    foFind.foldLeft(Map[(String, String), List[String]]()) {
      (map, pair) => {
        map(pair) = singleOrPair(
          findStringOfLastApperence(lines, pair._1),
          findStringOfLastApperence(lines, pair._2)
          )
      }
    }
  }

  def singleOrPair(one: String, two: String): List[String] = {
    if (one == two) List[String](one)
    else List[String](one, two)
  }

  def findStringOfLastApperence(lines: List[String], toFind: String): String = {
    lines.reverse.filter(line => individualWords(line).contains(toFind)).head
  }

  private def findRhymingWords(line: List[String]): List[(String, String)] = {
    val words = individualWords(line)
    //println("combos: "+buildCombinationPairs(words))
    //println("words = "+words)
    line.foreach(l => {})

    /*val rhymes:List[(String, String)] = {*/
    for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j
  }

  /**
   * finds the rhymes in one line
   */
  //  private def findRhymingWords(line: String): List[String] = {
  //    val words = individualWords(line)
  //    for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j
  //  }


//  def buildCombinationPairs(words: List[String]): List[(String, String)] = {
//    if (words.size == 2) List(words(0) -> words(1))
//    else {
//      println("tail: " + words.tail)
//      val list: List[(String, String)] = buildCombinationPairs(words.tail)
//      println("make List: " + list)
//      val list2: List[(String, String)] = list ::: words.tail.foldLeft(List[(String, String)]()) {
//        (pairs, word) => {
//          words.head -> word :: pairs
//        }
//      }
//      println("added List: " + list2)
//      list2
//    }
//  }
}