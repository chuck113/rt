package com.rt.indexing.persistence

import java.io._
import xml.{Utility, XML, Elem}
import sjson.json.{JsBean, DefaultConstructor, Serializer}
import reflect.BeanInfo
import dispatch.json.{Js, JsString, JsValue}
import org.apache.commons.io.IOUtils

@BeanInfo
  private case class ArtistAlbumsJson(artist: String, albums: Map[String, String]){
    private def this() = this(null, null)
  }

class ArtistAlbums(val artist: String, val albums: List[Album]){

  private val jsonPersister = new JsonPersister();
  val jsBean = new Object with JsBean with DefaultConstructor

  // needed for sjson, TODO not sure why
  implicit def ignoreProps = List[String]("class")

  def writeToFolder(file:String):Unit ={
    jsonPersister.writeToFile(file+"/"+Constants.artistMetaDataFileName, toJson())
  }

  def write(out:Writer):Unit ={
    jsonPersister.write(out, toJson())
  }

  override def toString():String ={
    "ArtistAlbums ["+artist+", "+albums+"]"
  }

  def toJson():String ={
     val jsonAlbums:Map[String, String] = albums.foldLeft(Map[String, String]()){(map, album)=>{
       map(album.fileInfo.albumName) = FileNameUtils.toFileName(album.fileInfo.fileName)
     }}

    val aaJson:ArtistAlbumsJson = ArtistAlbumsJson(artist, jsonAlbums)
    println("aaJson: "+aaJson)
    jsBean.toJSON(aaJson)
  }
}

object ArtistAlbums{
  val jsBean = new Object with JsBean with DefaultConstructor

  def fromString(json:String):ArtistAlbums ={
     fromJson(json)
  }

  def fromFolder(folder:String):ArtistAlbums ={
     fromJson(IOUtils.toString(new FileInputStream(folder+"/"+Constants.artistMetaDataFileName)))
  }

  def fromJson(json:String):ArtistAlbums ={
    val aaJson:ArtistAlbumsJson = jsBean.fromJSON(Js(json), Some(classOf[ArtistAlbumsJson])).asInstanceOf[ArtistAlbumsJson]
    val albums:List[Album] = aaJson.albums.foldLeft(List[Album]()){(list, albumEntry) => {
      new Album("id", new AlbumFile(albumEntry._2, albumEntry._1), null) :: list
    }}

    new ArtistAlbums(aaJson.artist, albums)
  }
}