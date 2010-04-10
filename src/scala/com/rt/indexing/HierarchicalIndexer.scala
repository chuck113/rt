package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import com.rt.dto.DataMapper
import persistence.{AlbumTrack, AlbumMetaData, ArtistAlbums, Constants}
import collection.immutable.Map1
import com.rt.rhyme.{Rhyme, RapSheetReader}
import java.io.{File, Serializable, FileOutputStream}
import com.rt.util.{NameMapper, MapUtils}
import org.apache.commons.io.FileUtils
//import util.ZipUtil


case class RhymeLeaf(word:String, lines: List[String], parts:List[String], rating:Int) extends Serializable
case class SongNode(title:String, trackNo:Int, rhymes: List[RhymeLeaf]) extends Serializable
case class AlbumNode(artist:String, title:String, year:Int, children: List[SongNode]) extends Serializable
case class ArtistNode(children: List[AlbumNode]) extends Serializable

class ArtistFolderNotFoundException(msg:String) extends RuntimeException(msg)

class HierarchicalIndexer(){

  private def checkFoldersExist(artists:List[String]){
    artists.foreach(artist => {
      if(!new File(OhhlaConfig.rawTargetLocation + "/" + artist).exists){
        throw new ArtistFolderNotFoundException("for artist '"+artist+"'")
      }
    })
  }

  def makeArtistsHierarchy(artists:List[String]):Map[String, ArtistNode]={
    checkFoldersExist(artists)
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

  /**
   * Used for testing when we find an error in a rhyme in a song
   */
  def makeAlbumNodeForOneSong(albumFolder:String, trackNumber:Int):AlbumNode={
    val md:AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val file = albumFolder + "/" + trackNumber + ".txt"
    val node: SongNode = makeSongNode(file, makeSongMetaData(md, AlbumTrack("title", trackNumber, "url")))
    printSongNodesDetailed(List(node), "")
    AlbumNode(md.artist, md.title, md.year, List(node))
  }

  private def makeAlbumNode(albumFolder:String):AlbumNode={
    val md:AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val songNodes = md.tracks.foldLeft(List[SongNode]()){(list, track) =>{//(track => {
      val file = albumFolder + "/" + track.number + ".txt"
      makeSongNode(file, makeSongMetaData(md, track)) :: list
    }}
    printSongNodes(songNodes, albumFolder)
    AlbumNode(md.artist, md.title, md.year, songNodes)
  }

  private def printSongNodesDetailed(songs:List[SongNode], albumFolder:String)={
    songs.foreach(s => {
      s.rhymes.foreach(r =>{
        println("parts: "+r.parts)
        r.lines.foreach(l =>{println("line: "+l)})
      })
    })
  }

  private def printSongNodes(songs:List[SongNode], albumFolder:String)={
    songs.foreach(s => println(s.rhymes.size +": "+s.title +" "+getBestRhyme(s) +" - "+albumFolder+" "+s.rhymes.map(_.parts)))
  }

  private def getBestRhyme(songNode:SongNode):RhymeLeaf={
    if(songNode.rhymes.length == 0)null
    else songNode.rhymes.sort((s1, s2) => s1.rating > s2.rating).head
  }

  private def makeSongNode(trackFile:String, song:SongMetaData):SongNode={
    val rhymes: List[Rhyme] = indexTrack(trackFile, song)
    val leaves:List[RhymeLeaf] = rhymes.foldLeft(List[RhymeLeaf]()){(leafList, rhyme) =>{
      buildRhymeLeaves(rhyme) ::: leafList
    }}
    SongNode(song.title, song.track, leaves)
  }

  private def buildRhymeLeaves(rhyme:Rhyme):List[RhymeLeaf]={
    rhyme.parts.foldLeft(List[RhymeLeaf]()){(leafList, part) => {
      RhymeLeaf(part, rhyme.lines, rhyme.parts, rhyme.rating) :: leafList
    }}
  }

//  private def makeSongNodeOld(trackFile:String, song:SongMetaData):SongNode={
//    val rhymes: Map[String, List[RhymeLines]] = indexTrackOld(trackFile, song)
//    val leaves:List[RhymeLeaf] = rhymes.foldLeft(List[RhymeLeaf]()){(leafList, rhymeLineEntry) =>{
//      println("got rhyme leaf: "+rhymeLineEntry._1+" of size "+rhymeLineEntry._2.size)
//      //TODO, remove hack, only gets first entry
//      RhymeLeaf(rhymeLineEntry._1, rhymeLineEntry._2(0).lines, null) :: leafList
//    }}
//    SongNode(song.title, song.track, leaves)
//  }

//  def indexAlbum(album: AlbumMetaData, albumFolder:String): Map[String, List[RhymeLines]] = {
//    var indexMap: Map[String, List[RhymeLines]] = Map[String, List[RhymeLines]]()
//
//    album.tracks.foreach(track => {
//      val file = albumFolder + "/" + track.number + ".txt"
//      indexMap = MapUtils.mergeListMaps(indexMap, indexTrackOld(file, makeSongMetaData(album, track)))
//    })
//    Map[String, List[RhymeLines]](indexMap.toList: _*)
//  }

//  private def indexTrackOld(trackFile:String, song:SongMetaData):Map[String, List[RhymeLines]]={
//    RapSheetReader.findRhymesOld(trackFile, song);
//  }

  private def indexTrack(trackFile:String, song:SongMetaData):List[Rhyme]={
    RapSheetReader.findRhymes(trackFile, song);
  }

  def serializeHierarcy(hierarchy:Map[String, ArtistNode]):Unit ={
    val mapper = new DataMapper()
    hierarchy.foreach(artistEntry =>{
      val albums:List[AlbumNode] = artistEntry._2.children
      albums.foreach(album => {
        val file = Constants.serialisedIndexHierarchyPreZipFolder("-"+artistEntry._1+"-"+NameMapper.nUnder(album.title))
        println("writing artist album node '"+artistEntry._1+" - "+album.title+"' to "+file)
        val outputStream: FileOutputStream = new FileOutputStream(file)
        mapper.write(album, outputStream)
        outputStream.flush()
        outputStream.close()
      })
    })
    FileUtils.deleteQuietly(new File(Constants.serialisedIndexHierarchyZipFile))
    DataMapper.zip(Constants.serializedIndexesZipFolder, Constants.serialisedIndexHierarchyZipFile)
    new File(Constants.serializedIndexesZipFolder).deleteOnExit

    FileUtils.deleteDirectory(new File(Constants.gaeIndexFolder))
    new File(Constants.gaeIndexFolder).mkdirs
    FileUtils.deleteQuietly(new File(Constants.gaeIndexZipFile))

    FileUtils.copyFile(new File(Constants.serialisedIndexHierarchyZipFile), new File(Constants.gaeIndexZipFile))
    DataMapper.unzip(Constants.gaeIndexZipFile, Constants.gaeIndexFolder)
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