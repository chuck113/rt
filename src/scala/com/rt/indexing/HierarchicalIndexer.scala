package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import com.rt.dto.DataMapper
import com.rt.rhyme.RapSheetReader
import com.rt.util.MapUtils
import java.io.{Serializable, FileOutputStream}
import persistence.{AlbumTrack, AlbumMetaData, ArtistAlbums, Constants}
import collection.immutable.Map1


case class RhymeLeaf(word:String, lines: List[String]) extends Serializable
case class SongNode(title:String, trackNo:Int, rhymes: List[RhymeLeaf]) extends Serializable
case class AlbumNode(artist:String, title:String, year:Int, children: List[SongNode]) extends Serializable
case class ArtistNode(children: List[AlbumNode]) extends Serializable

class HierarchicalIndexer(){

  def makeArtistsHierarchy(artists:List[String]):Map[String, ArtistNode]={
    artists.foldLeft(Map[String, ArtistNode]()){(map,artist)=>
      map(artist) = makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist)
    }
  }

  def makeArtistHierarchy(artist:String):Map[String, ArtistNode]={
    new Map1(artist, makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist))
  }

  /**
   * builds a hiearchy for artists using the given albums. Artists and albums are given
   * to be the neuteralised names, currently the actual folders on disk
   */
  def makeArtistsHierarchy(artistAlbums:Map[String, List[String]]):Map[String, ArtistNode]={
    artistAlbums.foldLeft(Map[String, ArtistNode]()){(map,entry)=>
      map(entry._1) = makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + entry._1, entry._2))
    }
  }

  def makeArtistAlbumsHierarchy(artistFolder:String, albums:List[String]):ArtistNode={
    makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + artistFolder, albums))
  }

  def applyParentFolderNameToFolderNames(parentFolderName:String, list:List[String]):List[String]={
    list.map(parentFolderName+"/"+_)
  }

  private def makeArtistNode(artistFolder:String):ArtistNode={
    val artist = ArtistAlbums.fromFolder(artistFolder)
    ArtistNode(artist.albums.foldLeft(List[AlbumNode]()){(list, album) =>{
      makeAlbumNode(artistFolder + "/" + album.fileInfo.fileName) :: list
    }})
  }

  private def makeArtistNode(albumFolder:List[String]):ArtistNode={
    ArtistNode(albumFolder.foldLeft(List[AlbumNode]()){(list, albumFolder) =>{
      makeAlbumNode(albumFolder) :: list
    }})
  }

  private def makeAlbumNode(albumFolder:String):AlbumNode={
    val md:AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val songNodes = md.tracks.foldLeft(List[SongNode]()){(list, track) =>{//(track => {
      val file = albumFolder + "/" + track.number + ".txt"
      makeSongNode(file, makeSongMetaData(md, track)) :: list
    }}
    AlbumNode(md.artist, md.title, md.year, songNodes)
  }

  private def makeSongNode(trackFile:String, song:SongMetaData):SongNode={
    val rhymes: Map[String, List[RhymeLines]] = indexTrack(trackFile, song)
    val leaves:List[RhymeLeaf] = rhymes.foldLeft(List[RhymeLeaf]()){(leafList, rhymeLineEntry) =>{
      //TODO, remove hack, only gets first entry
      RhymeLeaf(rhymeLineEntry._1, rhymeLineEntry._2(0).lines) :: leafList
    }}
    SongNode(song.title, song.track, leaves)
  }

//  def indexAlbum(album: AlbumMetaData, albumFolder:String): Map[String, List[RhymeLines]] = {
//    var indexMap: Map[String, List[RhymeLines]] = Map[String, List[RhymeLines]]()
//
//    album.tracks.foreach(track => {
//      val file = albumFolder + "/" + track.number + ".txt"
//      indexMap = MapUtils.mergeListMaps(indexMap, indexTrack(file, makeSongMetaData(album, track)))
//    })
//    Map[String, List[RhymeLines]](indexMap.toList: _*)
//  }

  private def indexTrack(trackFile:String, song:SongMetaData):Map[String, List[RhymeLines]]={
    RapSheetReader.findRhymes(trackFile, song);
  }

  def serializeHierarcy(hierarchy:Map[String, ArtistNode]):Unit ={
    val mapper = new DataMapper()
    hierarchy.foreach(entry =>{
      val file = Constants.serialisedIndexHierarchyFolder("-"+entry._1)
      mapper.write(entry._2, new FileOutputStream(file))
    })
  }

  //TODO this is in Indxer too, put into abstract class
  private def makeSongMetaData(album: AlbumMetaData, track: AlbumTrack): SongMetaData = {
    new SongMetaData(track.title, album.artist, album.year, album.title, track.number)
  }

  /** for java compatibility */
  private def makeArtistHierarchySingle(artist:String):Map[String, ArtistNode]={
    makeArtistsHierarchy(List(artist))
  }
}