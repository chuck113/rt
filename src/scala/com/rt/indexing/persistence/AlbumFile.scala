package com.rt.indexing.persistence



class AlbumFile(val fileName:String, val albumName:String){
  def this(albumName:String)=this(FileNameUtils.toFileName(albumName), albumName)
}