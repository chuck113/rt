package com.rt.itunes

import reflect.BeanInfo
import sjson.json.{Serializer, JSONTypeHint}
import com.rt.util.IO
import annotation.target.field

@BeanInfo
case class Albums(@(JSONTypeHint @field) (value = classOf[Track]) results: List[Track]) {
  private def this() = this (null)

  def hasAlbums:Boolean = results.size > 0 

  def albumName(): String = {
    results.head.collectionName
  }

  private def filterNonSongs():List[Track]={
    results.filter(_.kind == "song")
  }

  def trackNames(): List[String] = {
    hasAlbums match {
      case false => List[String]()
      case true => filterNonSongs.filter(_.collectionName.equals(albumName)).sort((a, b) => a.trackNumber < b.trackNumber).map(_.trackName)
    }
  }
}

@BeanInfo
case class Track(artistId: Int, artistName: String, collectionName: String, collectionId:Int, trackName: String, trackNumber: Int, kind:String) {
  private def this() = this (-1, null, null, -1, null, -1, null)
}

class ItunesSerializer {
  def deserialize(jsonFile:String):Albums= {
    return deserializeFromString(IO.readLineFromClassPathFile(jsonFile))
  }

  def deserializeFromString(json:String):Albums= {
    Serializer.SJSON.in[Albums](json).asInstanceOf[Albums]
  }
}