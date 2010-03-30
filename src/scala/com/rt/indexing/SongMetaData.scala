package com.rt.indexing

import java.io.Serializable

case class SongMetaData(title: String, artist:String, year:Int, album:String, track:Int) extends Serializable{

}