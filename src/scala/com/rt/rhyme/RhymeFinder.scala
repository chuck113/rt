package com.rt.rhyme

import com.rt.util.MapUtils
import collection.mutable.{ListBuffer, Map => MutableMap}
import java.lang.String

/**
 * Data structure for storing rhyme parts as single entries.
 */
class RhymePartSet(val rhymeMap:RhymeMap) {
  private case class Entry(var parts: List[String]) {
    def addPart(part: String): Unit = {
      if(!parts.contains(part)){
        parts ::= part
      }
    }
  }

  private var entries: List[Entry] = List[Entry]()

  def addPart(part: String): Unit = {
    getEntryForPart(part) match {
      case Some(e) => {e.addPart(part)}
      case None => {entries = Entry(List(part)) :: entries}
    }
  }

  def allRhymes(): List[List[String]] = {
    entries.foldLeft(List[List[String]]()) {
      (list, entry) => {
        list + entry.parts
      }
    }
  }

  private def getEntryForPart(part: String): Option[Entry] = {
    entries.foreach(e => {
      e.parts.foreach(p => {
        if (rhymeMap.doWordsRhyme(part, p)) {
          return Some(e)
        }
      })
    })
    None
  }

  private def contains(entry:Entry, part:String):Boolean={
    entry.parts.exists(p => {rhymeMap.doWordsRhyme(part, p)})
  }

  def containsEntryFor(part: String): Boolean = {
    entries.exists(e => {contains(e, part)})

    //      entries.foreach(e =>{
    //        e.foreach(p =>{
    //          if(rhymeMap.doWordsRhyme(part, p){
    //
    //          }
    //        })
    //      })
  }
}

class RhymeList{
  private var rhymes:List[Rhyme] = List[Rhyme]()

  /** returns any rhyme objects whos parts are a subset of the parts in the given rhyme */
  def subsetOf(rhyme:Rhyme):List[Rhyme]={
    rhymes.filter(_.isSubsetOf(rhyme))
  }

  /** returns any rhyme objects whos parts are a superset of the parts in the given rhyme */
  def superSetOf(rhyme:Rhyme):List[Rhyme]={
    rhymes.filter(_.isSupersetOf(rhyme))
  }

  def addRhymes(rhymesToAdd:List[Rhyme])={
    rhymesToAdd.foreach(addRhyme)
  }

  def addRhyme(rhyme:Rhyme)={
    // if a supersets of the rhyme exists, don't add it
    // is a subsets of the rhyme exists, remove them all and add it
    if(superSetOf(rhyme).size == 0){
      //println("BEFORE: "+rhymes)
      //println("sub set is : "+subsetOf(rhyme))
      rhymes = rhymes -- subsetOf(rhyme)
      addInternal(rhyme)
      //println("AFTER: "+rhymes)
    }
  }

  def addInternal(rhyme:Rhyme)={
    if(!rhymes.contains(rhyme)){
      rhymes = rhyme :: rhymes
    }
    //println("rhymes are now: "+rhymes)
  }

  def allRhymes():List[Rhyme]={
    rhymes
  }
}

case class Rhyme(parts: List[String], lines: List[String]) {
  def addLine(line: String): Unit = {
    if (!lines.contains(line)) {
      line :: lines
    }
  }

  def rating():Int={
    parts.foldLeft(1){(n, st) => {n * st.length }}
  }

  /**
   * Checks if this rhyme is a subset of rhyme 'that'
   *
   * (SPARK, BARK) is a subset of (SPARK, BARK, PARK, DARK) as all of its values are
   * present. entries in operand must appear in parts.
   */
  def isSubsetOf(that:Rhyme):Boolean={
    //println("is "+parts+" a subset of "+that.parts+": " + (parts.forall(that.parts.contains))+", "+(containsOneValueInCommon(that.parts, parts)))
    parts.forall(that.parts.contains) && containsOneValueInCommon(that.parts, parts)
    // all of my parts must apper in that's parts
  }

  def isSupersetOf(that:Rhyme):Boolean={
    //println("is "+parts+" a super set of "+that.parts+": " + (that.parts.forall(this.parts.contains))+", "+(containsOneValueInCommon(that.parts, parts)))
    that.parts.forall(this.parts.contains) && containsOneValueInCommon(that.parts, parts)
  }

  private def containsOneValueInCommon(l1:List[String], l2:List[String]):Boolean={
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
class RhymeFinder(val rhymeMap:RhymeMap) {

  /**
   * returns a map of individualWords to line numbers. Each word is mapped to where it appears
   * in the given list. It may be rhymed more than once.
   */
  def findRhymesInLines(lines: List[String]): List[Rhyme] = {
    //MapUtils.joinMaps(
      //findMultiLineRhymes(lines)
      findMultiPartRhymes(lines) //,
      //findSingleLineRhymes(lines)
      //)
  }

  /**
   * Should return a list of objects, where the object is the lines that participate in the ryhyme and the
   * found rhyme parts.
   *
   * Assume individualWords on the current line that rhyme with individualWords 2 lines earlier with rhyme with a word in the
   * intermediate line
   *
   */
  def findMultiPartRhymes(lines: List[String]): List[Rhyme] = {
    val foundRhymes:RhymeList = new RhymeList();
    var lastLineSearched = ""

    for (line <- lines) {
      val rhymes: List[Rhyme] = findUniqueRhymes(List(lastLineSearched, line))
      foundRhymes.addRhymes(rhymes)
      lastLineSearched = line
    }
    foundRhymes.allRhymes
  }

  // (SPARK, BARK) is a subset of (SPARK, BARK, PARK, DARK) as all of its values are
  // present. entries in operand must appear in parts.
  private def isPartSetASubSetOf(parts: List[String], operand:List[String]):Boolean={
    operand.forall(parts.contains(_))
  }

  /**
   * Used to determine wheather or not the ryhyme should be entered as as result, if
   * it is an intersecting rhyme already exsists it shouldnt' be
   */
//  def isRhymeContainedInAnElement(rhymes: List[Rhyme], rhymeToFind: Rhyme) = {
//    rhymes.exists(v => isRhymeContainedIn(rhymeToFind, v))
//  }

  /**
   * The strings in query are all present in rhyme
   */
//  def isRhymeContainedIn(rhyme: Rhyme, query: Rhyme): Boolean = {
//    println("rhyme is " + rhyme + ", query is " + query + ", res: " + (rhyme.parts.intersect(query.parts).size == query.parts.size && rhyme.lines.intersect(query.lines).size == query.lines.size))
//    rhyme.parts.intersect(query.parts).size == query.parts.size &&
//            rhyme.lines.intersect(query.lines).size == query.lines.size
//  }

  /**
   * If the list contains a rhyme that is an intersection of the given rhyme,
   * replace it with the new rhyme
   */
//  def replaceIntersectingRhyme(rhymes: List[Rhyme], rhyme: Rhyme): List[Rhyme] = {
//    rhymes.find(x => {isRhymeContainedIn(rhyme, x)}) match { //.flatMap(r => rhymes.remove(_ == r))
//      case None => rhymes
//      case Some(r) => {
//        rhyme :: rhymes.remove(_ == r)
//      }
//    }
//  }

  // returns a list of lines mapped to a list of rhyme parts for the given lines
  def findUniqueRhymes(lines: List[String]): List[Rhyme] = {
    //println("raw rhymes: " + findRhymePairsInLine(lines))
    val rhymes: List[List[String]] = getRhymes(findRhymePairsInLine(lines))
    //println("findUniqueRhymes rhymes: "+rhymes)
    findLinesContainingRhymesSets(rhymes, lines)
  }


  def findLinesContainingRhymesSets(sets: List[List[String]], lines:List[String]):List[Rhyme]={
    sets.foldLeft(List[Rhyme]()){(rhymeList, rhymeParts) =>{
      val linesContainingRhymeParts: List[String] = findLinesContainingRhymes(rhymeParts, lines)
      Rhyme(rhymeParts, linesContainingRhymeParts) :: rhymeList
    }}
  }

  def findLinesContainingRhymes(parts: List[String], lines: List[String]): List[String] = {
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
    var uniqueRhymeParts: List[List[String]] = List[List[String]]()

    val rhymeSet:RhymePartSet = new RhymePartSet(rhymeMap)
    pairs.foreach(pair =>{
      rhymeSet.addPart(pair._1)
      rhymeSet.addPart(pair._2)
    })

    rhymeSet.allRhymes

    //    pairs.foldLeft(List[String]()) {
    //      (list, pair) => {
    //        if (!list.contains(pair._1)) pair._1 :: list
    //        if (!list.contains(pair._2)) pair._2 :: list
    //        else list
    //      }
    //    }
  }

  private def individualWords(line: String): List[String] = {
    line.split(" ").map(cleanWord).map(x => x.toUpperCase).toList
  }

  private def individualWords(lines: List[String]): List[String] = {
    val allWords = lines.foldLeft(List[String]()) {(list, line) => {line.split(" ").toList ::: list}}
    allWords.map(cleanWord).map(x => x.toUpperCase)
  }

//  def findSingleLineRhymes(lines: List[String]): Map[String, List[List[Int]]] = {
//    val iter = lines.elements.counted
//
//    iter.foldLeft(Map[String, List[List[Int]]]()) {
//      (map, line) => {
//        val rhymes: List[String] = findRhymesInLine(line)
//        if (rhymes.size != 0) println("found single rhymes: " + rhymes + " in " + line)
//        MapUtils.addEntry(map, rhymes, iter.count)
//      }
//    }
//  }

  /**
   *  finds the rhymes in one line
   */
  def findRhymesInLine(line: List[String]): List[String] = {
    val words = individualWords(line)

    // only need to return 'i' because the reverse will also rhyme
    for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i
  }

  def findRhymePairsInLine(line: List[String]): List[(String, String)] = {
    val words = individualWords(line)

    // only need to return 'i' because the reverse will also rhyme
    for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i -> j
  }

  /**
   * finds the rhymes in one line
   */
  def findRhymesInLine(line: String): List[String] = {
    val words = individualWords(line)

    // only need to return 'i' because the reverse will also rhyme
    for{i <- words; j <- words; if (rhymeMap.doWordsRhyme(i, j))} yield i
  }


  private def cleanWord(word: String): String = {
    def option = "[\\w']+".r.findFirstIn(word)
    option match {
      case None => ""
      case _ => option.get
    }
  }

  /**
   * Is the given string present in any String in the given string list
   */
  def containsInSubString(lines: List[String], st: String): Boolean = {
    !lines.forall(line => {
      line.contains(st)
    })
  }
}