package com.rt.indexing

import org.apache.commons.io.FileUtils
import collection.mutable.MultiMap
import persistence.{ArtistAlbums, AlbumTrack, AlbumMetaData}
import xml.{XML, Elem}
import com.rt.ohhla.OhhlaConfig
import java.lang.String
import collection.immutable.Map
import java.io.{Serializable, FileInputStream, FileFilter, File}
import com.rt.rhyme.RapSheetReader
import com.rt.util.MapUtils


/**holds one or more rhyme lines and associated data about the containing track */
//case class RhymeData(albumData: AlbumMetaData, trackData: AlbumTrack, lines: RhymeLines)
//case class TrackMetaData(title: String, number: Int, albumData: AlbumMetaData)

case class RhymeIndexEntry(rhymes: List[RhymeLines], score: Int)

// One rhyme stored as a list of individualWords that rhyme, and data about the song it was found in.
case class RhymeLines(val song:SongMetaData, val lines: List[String]){

  def linesAsJavaList():java.util.List[String]={
    MapUtils.toJavaList(lines)
  }
}

class Indexer {

  def dirFilter = new FileFilter {
    def accept(f: File) = f.isDirectory
  }

  def textFileFilter = new FileFilter {
    def accept(f: File) = f.getName.endsWith(".txt")
  }

  def textFilesInFolder(dir: File): List[File] = {
    dir.listFiles(textFileFilter).toList
  }

  def foldersInDir(dir: File): List[File] = {
    dir.listFiles(dirFilter).toList
  }

  def filesInDir(dir: File): List[File] = {
    dir.listFiles().toList
  }


  //  private def buildAlbumMetaData(metaData: Elem):AlbumMetaData = {
  //    println("elem is "+metaData)
  //    new AlbumMetaData(
  //      (metaData \\ "@artist").text,
  //      (metaData \\ "@title").text,
  //      (metaData \\ "@year").text
  //    )
  //  }

  def indexArtists(rootFolder: String, artistFolders: List[String]): Map[String, List[RhymeLines]] = {
    var indexMap: Map[String, List[RhymeLines]] = Map[String, List[RhymeLines]]()

    artistFolders.foreach(artistFolder => {
      indexMap = MapUtils.mergeListMaps(indexMap, indexArtist(rootFolder + "/" + artistFolder))
    })
    indexMap
  }

  def indexArtist(artistAlbums:ArtistAlbums, artistFolder: String): Map[String, List[RhymeLines]] = {
    var indexMap: Map[String, List[RhymeLines]] = Map[String, List[RhymeLines]]()
    val artist = ArtistAlbums.fromFolder(artistFolder)

    println("ArtistAlbums = " + artist)
    artist.albums.foreach(album => {
      indexMap = MapUtils.mergeListMaps(indexMap, indexAlbum(artistFolder + "/" + album.fileInfo.fileName))
    })
    indexMap
  }

  private def getArtistAlbums(artistFolder:String):Option[ArtistAlbums]={
    try{
      Some(ArtistAlbums.fromFolder(artistFolder))
    }catch {
      case e:Exception => println("did not load artist albums from '"+artistFolder+"' due to "+e.getMessage);None

    }
  }

  def indexArtist(artistFolder: String): Map[String, List[RhymeLines]] = {
    var indexMap: Map[String, List[RhymeLines]] = Map[String, List[RhymeLines]]()
    val artist = ArtistAlbums.fromFolder(artistFolder)

    println("ArtistAlbums = " + artist)
    artist.albums.foreach(album => {
      indexMap = MapUtils.mergeListMaps(indexMap, indexAlbum(artistFolder + "/" + album.fileInfo.fileName))
    })
    indexMap
  }

  def getAlbumMetaData(albumFolder:String):Option[AlbumMetaData] = {
    try {
      Some(AlbumMetaData.fromFolder(albumFolder))
    } catch {
      case e: Exception => println("failed to read from album folder '"+albumFolder+"'" + e.getMessage); None
    }

  }

  def indexAlbum(albumFolder: String): Map[String, List[RhymeLines]] = {
      getAlbumMetaData(albumFolder) match{
        case None => Map[String, List[RhymeLines]]()
        case Some(d) => indexAlbum(d, albumFolder)
      }
  }

//  val artist = ArtistAlbums.fromFolder(artistFolder)
//
//    println("ArtistAlbums = " + artist)
//    artist.albums.foreach(album => {
//      indexMap = MapUtils.mergeListMaps(indexMap, indexAlbum(artistFolder + "/" + album.fileInfo.fileName))
//    })
//    indexMap

//  def makeArtistNode(albumFolder:List[String]):ArtistNode={
//    ArtistNode(albumFolder.foldLeft(List[AlbumNode]()){(list, albumFolder) =>{
//      println("for albumFolder "+albumFolder)
//      makeAlbumNode(albumFolder) :: list
//    }})
//  }
//
//  def makeArtistNode(artistFolder:String):ArtistNode={
//    val artist = ArtistAlbums.fromFolder(artistFolder)
//    ArtistNode(artist.albums.foldLeft(List[AlbumNode]()){(list, album) =>{
//      println("for artist "+artist)
//      makeAlbumNode(artistFolder + "/" + album.fileInfo.fileName) :: list
//    }})
//  }
//
//  def makeAlbumNode(albumFolder:String):AlbumNode={
//    val md:AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
//    val songNodes = md.tracks.foldLeft(List[SongNode]()){(list, track) =>{//(track => {
//      val file = albumFolder + "/" + track.number + ".txt"
//      makeSongNodeOld(file, makeSongMetaData(md, track)) :: list
//    }}
//    AlbumNode(md.artist, md.title, md.year, songNodes)
//  }
//
//  def makeSongNodeOld(trackFile:String, song:SongMetaData):SongNode={
//    val rhymes: Map[String, List[RhymeLines]] = indexTrackOld(trackFile, song)
//    val leaves:List[RhymeLeaf] = rhymes.foldLeft(List[RhymeLeaf]()){(leafList, rhymeLineEntry) =>{
//      //TODO, remove hack, only gets first entry
//      RhymeLeaf(rhymeLineEntry._1, rhymeLineEntry._2(0).lines) :: leafList
//    }}
//    SongNode(song.title, song.track, leaves)
//  }
//
  def indexAlbum(album: AlbumMetaData, albumFolder:String): Map[String, List[RhymeLines]] = {
    var indexMap: Map[String, List[RhymeLines]] = Map[String, List[RhymeLines]]()

    album.tracks.foreach(track => {
      val file = albumFolder + "/" + track.number + ".txt"
      indexMap = MapUtils.mergeListMaps(indexMap, indexTrack(file, makeSongMetaData(album, track)))
    })
    Map[String, List[RhymeLines]](indexMap.toList: _*)
  }

  def indexTrack(trackFile:String, song:SongMetaData):Map[String, List[RhymeLines]]={
    RapSheetReader.findRhymesOld(trackFile, song);
  }

  def buildScoringIndex(indexMap: Map[String, List[RhymeLines]]): Map[String, List[RhymeIndexEntry]] = {
    indexMap.elements.foldLeft(Map[String, List[RhymeIndexEntry]]()) {
      (map, entry) => {
        map(entry._1) = (map.getOrElse(entry._1, List[RhymeIndexEntry]()) + RhymeIndexEntry(entry._2, wordScore(entry._1, entry._2.size)))
      }
    }
  }

  def indexAll(): Map[String, List[RhymeLines]] = {
    val all = foldersInDir(new File(OhhlaConfig.rawTargetLocation))
    var indexMap: collection.mutable.HashMap[String, List[RhymeLines]] = new collection.mutable.HashMap[String, List[RhymeLines]]()
    println("will index: " + all.toList)
    val dir = all.head
    //all.foreach(dir => {
    val albumFolders = foldersInDir(dir);
    albumFolders.foreach(albumFolder => {
      println("albumFolder = " + albumFolder)
      //val data: AlbumMetaData = buildAlbumMetaData(toElem(albumFolder+"/md.xml"))
      val album: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder.toString)
      println("AlbumMetaData = " + album)
      //val songMappingFile = albumFolder+"/md.xml"
      album.tracks.foreach(track => {
        println("track = " + track)
        val file = albumFolder + "/" + track.number + ".txt"
        val songIndex: Map[String, List[RhymeLines]] = RapSheetReader.findRhymesOld(file, makeSongMetaData(album, track));
        songIndex.foreach(entry => {
          indexMap += entry._1 -> (indexMap.getOrElse(entry._1, List[RhymeLines]()) ::: entry._2.toList)
        })
      })
      //        textFilesInFolder(albumFolder).foreach(f => {
      //          println(f)
      //          val songIndex: Map[String, List[RhymeLines]] = RapSheetReader.findRhymesOld(f.getAbsolutePath());
      //          songIndex.foreach(entry => {
      //             indexMap += entry._1 -> (indexMap.getOrElse(entry._1, List[RhymeLines]()) ::: entry._2.toList)
      //          })
      //        })
    })
    //})

    //println("index has is "+indexMap.size+" individualWords")
    //indexMap.foreach(entry => println("word is "+ entry._1+", has "+ entry._2.size+" entries"))
    val list: List[(String, List[RhymeLines])] = indexMap.elements.toList.sort(wordScoreSorter)
    list.foreach(entry => println("word is " + entry._1 + ", has " + entry._2.size + " entries (" + wordScore(entry._1, entry._2.size) + ")"))
    Map[String, List[RhymeLines]](indexMap.toList: _*)
  }

  private def makeSongMetaData(album: AlbumMetaData, track: AlbumTrack): SongMetaData = {
    new SongMetaData(track.title, album.artist, album.year, album.title, track.number)
  }

  private def wordScoreSorter(first: (String, List[RhymeLines]), second: (String, List[RhymeLines])): Boolean = {
    wordScore(first._1, first._2.size) < wordScore(second._1, second._2.size)
  }

  private def wordScore(word: String, entries: Int): Int = {
    (word.length * word.length) * entries
  }
}