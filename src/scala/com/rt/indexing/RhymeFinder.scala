package com.rt.indexing


/**
 * Finds rhymes in a given list of sentences/lines.
 */
class RhymeFinder{

  private val rhymeMap = CMUDict.getRhymeMap();

  /**
   *  returns a map of words to line numbers. Each word is mapped to where it appears
   * in the given list. It may be rhymed more than once.
   */
  def findRhymesInLines(lines: List[String]): Map[String, List[List[Int]]] = {
    //var resultIndex = scala.collection.mutable.Map[String, List[List[Int]]]()
    println("findRhymesInLines using "+lines)
    MapUtils.joinMaps(
      findMultiLineRhymes(lines) //,
      //findSingleLineRhymes(lines)
      )
  }


  def findMultiLineRhymes(lines: List[String]): Map[String, List[List[Int]]] = {
    var resultIndex = scala.collection.mutable.Map[String, List[List[Int]]]()

    val iter = lines.elements.counted
    var lastWordUppercase: String = null

    iter.foreach(line => {
      val wordUppercase = cleanWord(line.split(" ").last).toUpperCase

      if (lastWordUppercase == null) lastWordUppercase = wordUppercase
      else {
        //if ((word != lastWord) && rhymeMap.contains(word) && rhymeMap.contains(lastWord)) {
        if (compareWordsOnRhyme(wordUppercase, lastWordUppercase)) {
          println("findMultiLineRhymes for "+wordUppercase+" and "+lastWordUppercase+" with line "+line)
          //println("findMultiLineRhymes adding )
          resultIndex(wordUppercase.toLowerCase()) = resultIndex.getOrElse(wordUppercase, List[List[Int]]()) ::: List(List(iter.count - 1, iter.count))
          resultIndex(lastWordUppercase.toLowerCase()) = resultIndex.getOrElse(lastWordUppercase, List[List[Int]]()) ::: List(List(iter.count - 1, iter.count))
          lastWordUppercase = null
        } else {
          lastWordUppercase = wordUppercase
        }
      }
    })
    Map(resultIndex.toSeq: _*)
  }

  def findSingleLineRhymes(lines: List[String]): Map[String, List[List[Int]]] = {
    val iter = lines.elements.counted

    iter.foldLeft(Map[String, List[List[Int]]]()) {
      (map, line) => {
        val rhymes: List[String] = findRhymesInLine(line)
        if (rhymes.size != 0) println("found single rhymes: " + rhymes + " in " + line)
        MapUtils.addEntry(map, rhymes, iter.count)
      }
    }
  }


  /**
   * finds the rhymes in one line
   */
  def findRhymesInLine(line: String): List[String] = {
    val words = line.split(" ").map(cleanWord).toList

    // only need to return 'i' because the reverse will also rhyme
    for{i <- words; j <- words; if (compareWordsOnRhyme(i, j))} yield i
  }


  private def doLastRhymeAndPrecedingLettersEqual(pairs: List[(String, String)]): boolean = {
    val equals = pairs.takeWhile(pair => (pair._1 == pair._2)).map(_._1)
    equals.size match {
      case 0 => false
      case _ => equals.findIndexOf(isRhymePart _) != -1
    }
  }

  private def isRhymePart(part: String): boolean = {
    part.charAt(part.length - 1) match {
      case '0' => true
      case '1' => true;
      case '2' => true;
      case _ => false
    }
  }

  /**
   * Given 2 lines find out if they rhyme by looking at
   * the last word in each line, Words must be in uppercase
   */
  private def compareWordsOnRhyme(one: String, two: String): boolean = {
    val oneUpper = one.toUpperCase()
    val twoUpper = two.toUpperCase()
    if ((oneUpper != twoUpper) && (rhymeMap.contains(oneUpper) && rhymeMap.contains(twoUpper))) {
      val it1 = rhymeMap(oneUpper).split(" ").reverse.elements
      val it2 = rhymeMap(twoUpper).split(" ").reverse.elements

      doLastRhymeAndPrecedingLettersEqual(it1.zip(it2).toList)
    } else {
      false
    }
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