package com.rt.rhyme

import com.rt.util.Strings


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
    replaceIns(Strings.removePunctuation(word))
  }


  def replaceIns(st:String):String={
    if(st.endsWith("IN'"))st.substring(0, st.length-3)+"ING"
    else st
  }

  def individualWords(line: String): List[String] = {
    // split the lines into words, clean each word, filter out any words that are now of 0 length
    // after the clean such as '-' and convert to uppercase
    line.split(" ").map(cleanWord).filter(_.length > 0).map(x => x.toUpperCase).toList
  }

  //TODO not finished or used anywhere
  def removeBrackets(line:String):String={
    //val tracksRegex = tracksStart + "[\\s\\S]+?" + tracksEnd
    //val tracksIter = tracksRegex.r.findAllIn(albumHtml)
    //val Name = """(.+?) - (.+?) [\\(\\)](.+?)[\\)\\)]""".r

    //val regex = """(.+?) \\((.+?)\\) (.+?)""".r
    //val regex = "[\\(]".r
    //regex.findAllIn(line);
    //println("find all in "+regex.findAllIn(line))
    //null
    //line match {
    //  case
      //case Name(a, t, y) => Some(new AlbumMetaData(a, t, removeNonNumberChars(y).toInt, tracks))
      //case _ => LOG.warn("could not get album meta data from string '" + htmlTitle + "'"); None
    //}

    val open ='('
    val close =')'
    if(line.contains(open) && line.contains(close)){
      if(line.indexOf(open) < line.indexOf(close)){
        (line.substring(0, line.indexOf(open)).trim +" " + line.substring(line.indexOf(close)+1).trim).trim
      }else{
        line
      }
    }else{
      line
    }
  }

  def individualWords(lines: List[String]): List[String] = {
    lines.foldLeft(List[String]()) {(list, line) => {individualWords(line) ::: list}}
  }
}