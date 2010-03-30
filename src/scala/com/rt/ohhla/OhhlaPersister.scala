package com.rt.ohhla

import java.io._
import xml.{XML, PrettyPrinter}
import io.Source
import org.apache.commons.io.IOUtils
import java.net.URL
import com.rt.indexing.persistence.{FileNameUtils, Album, ArtistAlbums, AlbumTrack}
import com.rt.util.NameMapper

/**
* Download rap sheets from ohhla 
*/
class OhhlaPersister{

  def persistArtistFiles(info: ArtistAlbums):Unit ={
    val artistAlbumsList: List[ArtistAlbums] = buildActualArtistAlbums(info)

    artistAlbumsList.foreach(artistAlbums => {
      val targetFolder = OhhlaConfig.rawTargetLocation+"/"+NameMapper.nUnder(artistAlbums.artist)
      new File(targetFolder).mkdirs
      artistAlbums.writeToFolder(targetFolder)
      artistAlbums.albums.foreach(album => {
        persistAlbum(album, targetFolder)
      })
    })
  }

  private def buildActualArtistAlbums(info: ArtistAlbums):List[ArtistAlbums]={
     val artistAlbumsMap:Map[String, List[Album]] = info.albums.foldLeft(Map[String, List[Album]]()){(map, album) => {
      val artist:String = album.metaData.artist
      map(artist) = new Album(album.metaData) :: map.getOrElse(artist, List[Album]())
    }}

    artistAlbumsMap.foldLeft(List[ArtistAlbums]()){(list, e) => {
      new ArtistAlbums(e._1, e._2) :: list
    }}
  }

//  private def persistAlbums(albums: List[Album], targetArtistFolder: String):Unit = {
//    val targetFolder = OhhlaConfig.rawTargetLocation+"/"+NameMapper.neutralizeWithUnderscores()
//    albums.foreach(album => persistAlbum(album, targetArtistFolder))
//  }


  private def persistAlbum(album: Album, targetArtistFolder: String):Unit = { // return xml
    val targetFolder = targetArtistFolder+"/"+NameMapper.nUnder(album.fileInfo.fileName)//toFileName(album.metaData.title)
    new File(targetFolder).mkdirs;
    album.metaData.writeToFolder(targetFolder)
    downloadTracks(album.metaData.tracks, targetFolder, OhhlaConfig.ohhlaUrl)
  }

  private def downloadTracks(tracks: List[com.rt.indexing.persistence.AlbumTrack], targetFolder: String, ohhlaRootUrl: String): Unit = {
    println("will download "+tracks+" to "+targetFolder)
    //val iter = tracks.elements.counted
    tracks.foreach(track => {
        val fileName = targetFolder+"/"+track.number+".txt"
        println("starting download for "+ohhlaRootUrl+"/"+track.url +" to " + fileName)
        if(!new File(fileName).exists){

          //IOUtils.copy(new URL(OhhlaConfig.ohhlaUrl+"/"+track.url).openStream, new FileWriter(fileName))
          val writer = new BufferedWriter(new FileWriter(fileName))
          //Source.fromURL(OhhlaConfig.ohhlaUrl+"/"+track.url).getLines.foreach(line =>{
          try{
            Source.fromURL(ohhlaRootUrl+"/"+track.url).getLines.foreach(line =>{
              writer.write(line)
            })
            writer.flush;writer.close();
          }catch {
            case e:Exception => println("did not save track from "+ohhlaRootUrl+"/"+track.url+" due to "+e.getMessage)
          }
        }else{
          println("skipping "+fileName+" as already exists")
        }
    })
  }
}