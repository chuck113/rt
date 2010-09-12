package com.rt.ohhla.persistence

import com.rt.util.NameMapper


case class AlbumFile(fileName:String, albumName:String){
  def this(albumName:String)=this(NameMapper.nUnder(albumName), albumName)
}