package com.rt.util



object NameMapper{
  private val toRemove:List[String] = List("\\", "/", "(", ")", ".", ":", ";", "*", "?", ",", "'", "\"", "-", "+", "!", "$", "%", "[", "]", "{", "}", "_", "=", "~");
  private val toReplace:List[(String, String)] = List(("&" -> "AND"))

  private def neutralizeWithSpaces(st:String):String={
    neuteralize(st, " ")
  }

  private def neutralizeWithNoSpaces(st:String):String={
    neuteralize(st, "")
  }

  private def neutralizeWithUnderscores(st:String):String={
    neuteralize(st, "_")
  }

  def nSpace(st:String):String={
    neutralizeWithSpaces(st)
  }

  def nNoSpace(st:String):String={
    neutralizeWithNoSpaces(st)
  }

  def nUnder(st:String):String={
    neutralizeWithUnderscores(st)
  }

  private def neuteralize(st:String, spaceChar:String):String={
    var result:String = st
    for(r <- toRemove){
      result = result.replace(r, "")
    }
    for(r <- toReplace){
      result = result.replace(r._1, r._2)
    }

    result.replace(" ", spaceChar).toUpperCase
  }
}