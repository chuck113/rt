package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import com.rt.dto.DataMapper
import persistence.{AlbumTrack, AlbumMetaData, ArtistAlbums, Constants}
import com.rt.rhyme.{Rhyme, RapSheetReader}
import java.io.{File, Serializable, FileOutputStream}
import com.rt.util.{NameMapper}
import org.apache.commons.io.FileUtils
import java.lang.String
import collection.immutable.{Map, Map1, Set => MutableSet}
import collection.mutable.{MultiMap, ListBuffer}
// This contains all of the items the iphone will need and are added after the
// object is created which is pretty but simple for the moment
case class RhymeLeaf(word: String, lines: List[String], parts: List[String], rating: Int, suitability:Int, artistScore:Int) extends Serializable{
  def this(word: String, lines: List[String], parts: List[String]) = this(word, lines, parts, 0, 0, 0)
}

case class SongNode(title: String, trackNo: Int, rhymes: List[RhymeLeaf]) extends Serializable
case class AlbumNode(artist: String, title: String, year: Int, children: List[SongNode]) extends Serializable
case class ArtistNode(children: List[AlbumNode]) extends Serializable

case class HierarchicalIndexerResult(index:Map[String, ArtistNode], foundWords:Set[String]){


  def bestRhymes():List[RhymeLeaf]={
    val sorted = rhymeList(index.values.toList).sort((a,b) => a.rating > b.rating)
    sorted.foreach(println)
    sorted
  }
  
   def rhymeList(artistNodes:List[ArtistNode]):List[RhymeLeaf]={
    val rhymeBuffer:ListBuffer[RhymeLeaf] = new ListBuffer[RhymeLeaf]();
    artistNodes.foreach(artist =>{
           artist.children.foreach(album =>{
             album.children.foreach(song =>{
               song.rhymes.foreach(rhyme =>{
                  rhymeBuffer.append(rhyme)
//                 rhyme.lines.foreach(line =>{
//                   out.println(line)
//                 })
                 //listBuffer.appendAll(rhyme.parts)
               })
             })
           })
        })
    rhymeBuffer.toList
  }
}

class ArtistFolderNotFoundException(msg: String) extends RuntimeException(msg)

class HierarchicalIndexer() {
  //def this()={this(Map[String, Int]())}

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
        map(artist) = makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist)
      }
    }
  }

  def makeArtistHierarchyWithAllWords(artists: List[String]): HierarchicalIndexerResult = {
    val hierarchy: Map[String, ArtistNode] = applyToRhymeParts(makeArtistsHierarchy(artists), RhymeScoreCalculator.calculate)    
    val allWords:Set[String] = Set() ++ getAllRhymeParts(hierarchy.values.toList)
    println("found "+allWords.size+" words")
    HierarchicalIndexerResult(hierarchy, allWords)
  }


  def applyToRhymeParts(song: SongNode, func:RhymeLeaf => RhymeLeaf):SongNode={
    new SongNode(song.title, song.trackNo, song.rhymes.map(func));
  }

  def applyToRhymeParts(album: AlbumNode, func:RhymeLeaf => RhymeLeaf):AlbumNode={
    new AlbumNode(album.artist, album.title, album.year, album.children.map(s => applyToRhymeParts(s, func)))
  }

  def applyToRhymeParts(artist: ArtistNode, func:RhymeLeaf => RhymeLeaf):ArtistNode={
    new ArtistNode(artist.children.map(a => applyToRhymeParts(a, func)))
  }

  def applyToRhymeParts(hierarchy: Map[String, ArtistNode], func:RhymeLeaf => RhymeLeaf):Map[String, ArtistNode]={
    hierarchy.foldLeft(Map[String, ArtistNode]()){(res, entry)=>{
      res(entry._1) = applyToRhymeParts(entry._2, func);
    }}
    //result: Map[String, ArtistNode]

//    hierarchy.values.foreach(artist =>{
//       artist.children.foreach(album =>{
//         album.children.foreach(song =>{
//
//           song.rhymes.foreach(rhyme =>{
//            RhyneLeaf newRhymeLeaf = func(rhyme)
//           })
//         })
//       })
//    })
  }

  def getAllRhymeParts(artistNodes:List[ArtistNode]):List[String]={
    val listBuffer:ListBuffer[String] = new ListBuffer[String]()
    artistNodes.foreach(artist =>{
      print(".")
       artist.children.foreach(album =>{
         album.children.foreach(song =>{
           song.rhymes.foreach(rhyme =>{
             listBuffer.appendAll(rhyme.parts)
           })
         })
       })
    })

    listBuffer.toList.removeDuplicates
  }

  def makeArtistHierarchy(artist: String): Map[String, ArtistNode] = {
    new Map1(artist, makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist))
  }

  /**
   * builds a hiearchy for artists using the given albums. Artists and albums are given
   * to be the neuteralised names, currently the actual folders on disk
   */
  def makeArtistsHierarchy(artistAlbums: Map[String, List[String]]): Map[String, ArtistNode] = {
    artistAlbums.foldLeft(Map[String, ArtistNode]()) {
      (map, entry) =>
        map(entry._1) = makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + entry._1, entry._2))
    }
  }

  def makeArtistAlbumsHierarchy(artistFolder: String, albums: List[String]): ArtistNode = {
    makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + artistFolder, albums))
  }

  def applyParentFolderNameToFolderNames(parentFolderName: String, list: List[String]): List[String] = {
    list.map(parentFolderName + "/" + _)
  }

  private def makeArtistNode(artistFolder: String): ArtistNode = {
    val artist = ArtistAlbums.fromFolder(artistFolder)
    ArtistNode(artist.albums.foldLeft(List[AlbumNode]()) {
      (list, album) => {
        makeAlbumNode(artistFolder + "/" + album.fileInfo.fileName) :: list
      }
    })
  }

  private def makeArtistNode(albumFolder: List[String]): ArtistNode = {
    ArtistNode(albumFolder.foldLeft(List[AlbumNode]()) {
      (list, albumFolder) => {
        makeAlbumNode(albumFolder) :: list
      }
    })
  }

  /**
   * Used for testing when we find an error in a rhyme in a song
   */
  def makeAlbumNodeForOneSong(albumFolder: String, trackNumber: Int): AlbumNode = {
    val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val file = albumFolder + "/" + trackNumber + ".txt"
    val node: SongNode = makeSongNode(file, makeSongMetaData(md, AlbumTrack("title", trackNumber, "url")))
    //printSongNodesDetailed(List(node), "")
    AlbumNode(md.artist, md.title, md.year, List(node))
  }

  private def makeAlbumNode(albumFolder: String): AlbumNode = {
    val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val songNodes = md.tracks.foldLeft(List[SongNode]()) {
      (list, track) => { //(track => {
        val file = albumFolder + "/" + track.number + ".txt"
        makeSongNode(file, makeSongMetaData(md, track)) :: list
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
    songs.foreach(s => println(s.rhymes.size + ": " + s.title + " - " + albumFolder + " " + s.rhymes.map(_.parts)))
  }

//  private def getBestRhyme(songNode: SongNode): RhymeLeaf = {
//    if (songNode.rhymes.length == 0) null
//    else songNode.rhymes.sort((s1, s2) => s1.rating > s2.rating).head
//  }

  private def makeSongNode(trackFile: String, song: SongMetaData): SongNode = {
    val rhymes: List[Rhyme] = indexTrack(trackFile, song)
    val leaves: List[RhymeLeaf] = rhymes.foldLeft(List[RhymeLeaf]()) {
      (leafList, rhyme) => {
        buildRhymeLeaves(rhyme) ::: leafList
      }
    }
    SongNode(song.title, song.track, leaves)
  }

  private def buildRhymeLeaves(rhyme: Rhyme): List[RhymeLeaf] = {
    rhyme.parts.foldLeft(List[RhymeLeaf]()) {
      (leafList, part) => {
        new RhymeLeaf(part, rhyme.lines, rhyme.parts) :: leafList
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