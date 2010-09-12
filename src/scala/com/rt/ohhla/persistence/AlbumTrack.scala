package com.rt.ohhla.persistence

import reflect.BeanInfo


@BeanInfo
case class AlbumTrack(title: String, number: Int, url: String){
  private def this() = this(null, -1, null)
  
  override def toString():String ={
    "AlbumTrack ["+title+", "+number+", "+url+"]"
  }
}