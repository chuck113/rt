package com.rt.rhyme

import com.rt.util.Strings
import java.lang.String


/**
 * Functions needed for processing rhymes, kept here so the same rules can be
 * applied by different processors
 */
object StringRhymeUtils{

  def cleanWord(word: String): String = {
    def option = "[\\w']+".r.findFirstIn(word)
    option match {
      case None => ""
      case _ => option.get
    }
  }

  def prepareWordForComparison(word:String):String={
    Strings.removePunctuation(word)
  }

  def individualWords(line: String): List[String] = {
    // split the lines into words, clean each word, filter out any words that are now of 0 length
    // after the clean such as '-' and convert to uppercase
    line.split(" ").map(cleanWord).filter(_.length > 0).map(x => x.toUpperCase).toList
  }

  def removeBrackets(line:String):String={
    removeBrackets(removeBrackets(removeBrackets(line, "[", "]"), "{", "}"), "(", ")")
  }

  def containBrakets(line:String, open:String, close:String):Boolean={
    (line.contains(open) && line.contains(close)) &&
    (line.indexOf(open) < line.indexOf(close))
  }

  def removeBrackets(line:String, open:String, close:String):String={
    if(containBrakets(line, open, close)){
        (line.substring(0, line.indexOf(open)).trim +" " + line.substring(line.indexOf(close)+1).trim).trim
    }else{
      line
    }
  }

  def individualWords(lines: List[String]): List[String] = {
    lines.foldLeft(List[String]()) {(list, line) => {individualWords(line) ::: list}}
  }


  def areLinesSimilar(one:List[String], two:List[String]):Boolean ={
    val oneWords: Set[String] = Set() ++ individualWords(one).filter(_.length > 2)
    val twoWords: Set[String] = Set() ++ individualWords(two).filter(_.length > 2)

    if(individualWords(one).size < 10 || individualWords(two).size < 10 ){
      false
    }else{
      //if contains 90% of words over 3 letters, they are the same
      val simialr:Boolean = oneWords.intersect(twoWords).size > (Math.min(oneWords.size, twoWords.size).toDouble * 0.8).toInt
      if(simialr){
        println("discovered similar lines "+one+" and "+two)
      }

      simialr
    }
  }
}