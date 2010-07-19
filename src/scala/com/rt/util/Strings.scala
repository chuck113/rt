package com.rt.util



object Strings{
  def removePunctuation(st:String):String={
    val toRemove:List[String] = List("\\", "/", "(", ")", ".", ":", ";", "*", "?", ",", "'", "\"", "-", "+", "!", "$", "%", "[", "]", "{", "}", "_", "=", "~");
    var result:String = st
    for(r <- toRemove){
      result = result.replace(r, "")
    }
    result
  }

  def replaceAmpersand(st:String):String={
    st.replace("&", "AND")
  }
}