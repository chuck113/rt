package com.rt.indexing.persistence



object FileNameUtils{

  def toFileName(st: String):String ={
    val toReplace:List[String] = List(" ", "\\", "/", ".", ":", ";", "*", "?", ",", "'", "\"");
    var result:String = st
    for(r <- toReplace){
      result = result.replace(r, "_") 
    }
    result
    //st.replace(" ", "_").replace("\\", "_").replace("/", "_").replace(".", "_").replace(":", "_").replace(";", "_").replace("*", "_S").replace("?", "_Q")
  }
}