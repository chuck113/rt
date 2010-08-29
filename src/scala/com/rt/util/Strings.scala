package com.rt.util

object Strings{

  private val toRemove:List[String] = List("\\", "/", "(", ")", ".", ":", ";", "*", "?", ",", "'", "\"", "-", "+", "!", "$", "%", "[", "]", "{", "}", "_", "=", "~");
  private val toRemoveSet:Set[String] = Set[String]() ++ toRemove

  def removePunctuation(st:String):String={
    var result:String = st
    for(r <- toRemove){
      result = result.replace(r, "")
    }
    result
  }

  def trimPunctuation(st:String):String={
    if(st == null || st.length == 0)return ""

    val str = trimPunctuationFromStart(st.trim.reverse)
    return trimPunctuationFromStart(str.reverse)
  }

  private def trimPunctuationFromStart(st:String):String={
    if(st == null || st.length == 0)return ""

    var ms:String = st;

    while(toRemoveSet.contains(ms.charAt(0).toString)){
      ms = ms.substring(1, ms.length)
      if(ms.length == 0)return ""
    }
    return ms;
  }

  def replaceAmpersand(st:String):String={
    st.replace("&", "AND")
  }
}