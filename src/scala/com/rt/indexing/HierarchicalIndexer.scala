package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import com.rt.dto.DataMapper
import persistence.{AlbumTrack, AlbumMetaData, ArtistAlbums, Constants}
import collection.immutable.Map1
import com.rt.rhyme.{Rhyme, RapSheetReader}
import java.io.{File, Serializable, FileOutputStream}
import com.rt.util.{NameMapper, MapUtils}
import org.apache.commons.io.FileUtils

case class RhymeLeaf(word: String, lines: List[String], parts: List[String], rating: Int, artistScore:Int) extends Serializable
case class SongNode(title: String, trackNo: Int, rhymes: List[RhymeLeaf]) extends Serializable
case class AlbumNode(artist: String, title: String, year: Int, children: List[SongNode]) extends Serializable
case class ArtistNode(children: List[AlbumNode]) extends Serializable

class ArtistFolderNotFoundException(msg: String) extends RuntimeException(msg)

class HierarchicalIndexer(val artistScores:Map[String, Int]) {
  def this()={this(Map[String, Int]())}

  class ProgressPrinter{
    var count = 0
    val newLineCount = 50
    val tenChar = "X"

    def done(){println()}

    def inc(){
      if(count == 0){
        print(".")
      }else if(count % newLineCount == 0){
        println(".("+newLineCount+")")
      }else if(count % 10 == 0){
        println(tenChar)
      }else{
        print(".")
      }
    }
  }

  private def checkFoldersExist(artists: List[String]) {
    artists.foreach(artist => {
      if (!new File(OhhlaConfig.rawTargetLocation + "/" + artist).exists) {
        throw new ArtistFolderNotFoundException("for artist '" + artist + "'")
      }
    })
  }

  def makeArtistsHierarchy(artists: List[String]): Map[String, ArtistNode] = {
    checkFoldersExist(artists)
    val printer:ProgressPrinter = new ProgressPrinter()
    artists.foldLeft(Map[String, ArtistNode]()) {
      (map, artist) => {
        printer.inc
        map(artist) = makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist, artistScores.getOrElse(artist,0))
      }
    }
  }

  def makeArtistHierarchy(artist: String): Map[String, ArtistNode] = {
    new Map1(artist, makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist, artistScores.getOrElse(artist,0)))
  }

  /**
   * builds a hiearchy for artists using the given albums. Artists and albums are given
   * to be the neuteralised names, currently the actual folders on disk
   */
  def makeArtistsHierarchy(artistAlbums: Map[String, List[String]]): Map[String, ArtistNode] = {
    artistAlbums.foldLeft(Map[String, ArtistNode]()) {
      (map, entry) =>
        map(entry._1) = makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + entry._1, entry._2), artistScores.getOrElse(entry._1,0))
    }
  }

  def makeArtistAlbumsHierarchy(artistFolder: String, albums: List[String], artistScore:Int): ArtistNode = {
    makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + artistFolder, albums), artistScore)
  }

  def applyParentFolderNameToFolderNames(parentFolderName: String, list: List[String]): List[String] = {
    list.map(parentFolderName + "/" + _)
  }

  private def makeArtistNode(artistFolder: String, artistScore:Int): ArtistNode = {
    val artist = ArtistAlbums.fromFolder(artistFolder)
    ArtistNode(artist.albums.foldLeft(List[AlbumNode]()) {
      (list, album) => {
        makeAlbumNode(artistFolder + "/" + album.fileInfo.fileName, artistScore) :: list
      }
    })
  }

  private def makeArtistNode(albumFolder: List[String], artistScore:Int): ArtistNode = {
    ArtistNode(albumFolder.foldLeft(List[AlbumNode]()) {
      (list, albumFolder) => {
        makeAlbumNode(albumFolder, artistScore) :: list
      }
    })
  }

  /**
   * Used for testing when we find an error in a rhyme in a song
   */
  def makeAlbumNodeForOneSong(albumFolder: String, trackNumber: Int): AlbumNode = {
    val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val file = albumFolder + "/" + trackNumber + ".txt"
    val node: SongNode = makeSongNode(file, makeSongMetaData(md, AlbumTrack("title", trackNumber, "url")),0)
    printSongNodesDetailed(List(node), "")
    AlbumNode(md.artist, md.title, md.year, List(node))
  }

  private def makeAlbumNode(albumFolder: String, artistScore:Int): AlbumNode = {
    val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val songNodes = md.tracks.foldLeft(List[SongNode]()) {
      (list, track) => { //(track => {
        val file = albumFolder + "/" + track.number + ".txt"
        makeSongNode(file, makeSongMetaData(md, track), artistScore) :: list
      }
    }
    //printSongNodes(songNodes, albumFolder)
    AlbumNode(md.artist, md.title, md.year, songNodes)
  }

  private def printSongNodesDetailed(songs: List[SongNode], albumFolder: String) = {
    songs.foreach(s => {
      s.rhymes.foreach(r => {
        println("parts: " + r.parts)
        r.lines.foreach(l => {println("line: " + l)})
      })
    })
  }

  private def printSongNodes(songs: List[SongNode], albumFolder: String) = {
    songs.foreach(s => println(s.rhymes.size + ": " + s.title + " " + getBestRhyme(s) + " - " + albumFolder + " " + s.rhymes.map(_.parts)))
  }

  private def getBestRhyme(songNode: SongNode): RhymeLeaf = {
    if (songNode.rhymes.length == 0) null
    else songNode.rhymes.sort((s1, s2) => s1.rating > s2.rating).head
  }

  private def makeSongNode(trackFile: String, song: SongMetaData, artistScore:Int): SongNode = {
    val rhymes: List[Rhyme] = indexTrack(trackFile, song)
    val leaves: List[RhymeLeaf] = rhymes.foldLeft(List[RhymeLeaf]()) {
      (leafList, rhyme) => {
        buildRhymeLeaves(rhyme, artistScore) ::: leafList
      }
    }
    SongNode(song.title, song.track, leaves)
  }

  private def buildRhymeLeaves(rhyme: Rhyme, artistScore:Int): List[RhymeLeaf] = {
    rhyme.parts.foldLeft(List[RhymeLeaf]()) {
      (leafList, part) => {
        RhymeLeaf(part, rhyme.lines, rhyme.parts, rhyme.rating, artistScore) :: leafList
      }
    }
  }

  private def indexTrack(trackFile: String, song: SongMetaData): List[Rhyme] = {
    RapSheetReader.findRhymes(trackFile, song);
  }

  def serializeRhymedWords(parts: List[String]): Unit = {
    FileUtils.deleteQuietly(new File(Constants.allRhymePartsFile))

    val mapper = new DataMapper()
    val outputStream: FileOutputStream = new FileOutputStream(Constants.allRhymePartsFile)
    mapper.write(parts, outputStream)
    outputStream.flush()
    outputStream.close()

    FileUtils.deleteQuietly(new File(Constants.gaeAllRhymePartsFile))
    FileUtils.copyFile(new File(Constants.allRhymePartsFile), new File(Constants.gaeAllRhymePartsFile))
    println("copying "+Constants.allRhymePartsFile+" to "+Constants.gaeAllRhymePartsFile)
  }

  def serializeHierarcy(hierarchy: Map[String, ArtistNode]): Unit = {
    FileUtils.deleteDirectory(new File(Constants.serializedIndexesZipFolder))
    new File(Constants.serializedIndexesZipFolder).mkdirs

    val mapper = new DataMapper()
    hierarchy.foreach(artistEntry => {
      val albums: List[AlbumNode] = artistEntry._2.children
      albums.foreach(album => {
        val file = Constants.serialisedIndexHierarchyPreZipFolder("-" + artistEntry._1 + "-" + NameMapper.nUnder(album.title))
        //println("writing artist album node '" + artistEntry._1 + " - " + album.title + "' to " + file)
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

  /**for java compatibility */
  private def makeArtistHierarchySingle(artist: String): Map[String, ArtistNode] = {
    makeArtistsHierarchy(List(artist))
  }
}