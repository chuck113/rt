package com.rt.ohhla

object ParsingUtils {

  def betweenFirstAndLastQuotes(line: String): String = {
    val first = line.indexOf("\"")
    val last = line.lastIndexOf("\"")
    line.substring(first + 1, last);
  }


  /**
   *  Given a line such as <a href="YFA_snoopdogg.html">Snoop Dogg</a>, will return (YFA_snoopdogg.html -> Snoop Dogg)
   */
  def hrefLinkAndtitle(hrefLine:String):(String, String) ={
    val firstQuote = hrefLine.indexOf("\"")+1
    val secondQuote = hrefLine.indexOf("\"", firstQuote+1)
    val firstCloseAngleBracket = hrefLine.indexOf(">", secondQuote)+1
    val secondOpenAngleBracket = hrefLine.indexOf("<", firstCloseAngleBracket+1)
    hrefLine.substring(firstQuote, secondQuote) -> hrefLine.substring(firstCloseAngleBracket, secondOpenAngleBracket)
  }

  def mostCommonValueInList[T](list: List[T]):T={
    val valsInList:List[T] = list.removeDuplicates
    val counts:List[Int] = valsInList.map(v => list.count(_ == v))
    val zipped:List[(Int,T)] = counts.zip(valsInList)
    zipped.sort(_._1 > _._1)(0)._2
  }
}