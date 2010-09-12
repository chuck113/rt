package com.rt.indexing

import com.rt.ohhla.OhhlaFiles
import com.rt.dto.DataMapper
import org.apache.commons.io.FileUtils
import java.lang.String
import collection.immutable.Map
import collection.mutable.{Map => MutableMap}
import java.io._
import com.rt.util.NameMapper
import collection.JavaConversions._
import com.rt.ohhla.persistence.Constants

trait Node extends Serializable{
  private val properties:MutableMap[String,String] = MutableMap()

  def getProp(key:String):Option[String]=properties.get(key)
  def setProp(key:String, value:String)=properties += key -> value
}

object RhymeLeaf{
  def apply(parent:SongNode)(word: String, lines: List[String], parts: List[String]):RhymeLeaf={
    new RhymeLeaf(parent, word, lines, parts)
  }
}

case class RhymeLeaf(parent:SongNode, word: String, lines: List[String], parts: List[String] = List(), rating:Int) extends Node {
  def this(parent:SongNode, word: String, lines: List[String], parts: List[String]) = this (parent, word, lines, parts, 0)

  def linesJList(): java.util.List[String] = {
    asList(lines.toSeq)
  }

  def partsJList(): java.util.List[String] = {
    asList(parts.toSeq)
  }
}

object SongNode{
  def apply(parent:AlbumNode)(title: String, trackNo: Int):SongNode={
    new SongNode(parent, title, trackNo)
  }
}

case class SongNode(parent:AlbumNode, title: String, trackNo: Int, rhymes: List[RhymeLeaf] = List()) extends Node {
  def rhymesJList(): java.util.List[RhymeLeaf] = {
    asList(rhymes.toSeq)
  }

  def addLeaves(childFactories: List[(SongNode => RhymeLeaf)]):SongNode={
    new SongNode(parent, title, trackNo, childFactories.map(_(this)))  
  }
}

object AlbumNode{
  def apply (parent:ArtistNode)(artist: String, title: String, year: Int):AlbumNode={
    new AlbumNode(parent, artist, title, year)
  }
}

case class AlbumNode(parent:ArtistNode, artist: String, title: String, year: Int, children: List[SongNode] = List()) extends Node {
  def childrenJList(): java.util.List[SongNode] = {
    asList(children.toSeq)
  }

  def addChildren(childFactories:List[(AlbumNode) => SongNode]):AlbumNode={
    new AlbumNode(parent, artist, title, year, childFactories.map(_(this)))
  }
}


case class ArtistNode(name:String, fileName:String, children: List[AlbumNode] = List()) extends Node {
  def childrenJList(): java.util.List[AlbumNode] = {
    asList(children.toSeq)
  }

  def addChildren(childFactories:List[(ArtistNode) => AlbumNode]):ArtistNode={
    new ArtistNode(name, fileName, childFactories.map(_(this)))
  }
}



class ArtistFolderNotFoundException(msg: String) extends RuntimeException(msg)

class HierarchicalIndexer() {

//  def makeArtistHierarchyWithAllWords(artists: List[String], rootFolder: String): HierarchicalIndexerResult = {
//    val hierarchy: Map[String, ArtistNode] = applyToRhymeParts(HierarchyBuilder.makeArtistsHierarchy(artists, rootFolder), RhymeScoreCalculator.calculate)
//    val allWords: Set[String] = Set() ++ getAllRhymeParts(hierarchy.values.toList)
//    HierarchicalIndexerResult(hierarchy, allWords)
//  }
//
//  def makeArtistHierarchyWithAllWords(artists: List[String]): HierarchicalIndexerResult = {
//    val hierarchy: Map[String, ArtistNode] = applyToRhymeParts(HierarchyBuilder.makeArtistsHierarchy(artists, OhhlaFiles.rawTargetLocation), RhymeScoreCalculator.calculate)
//    val allWords: Set[String] = Set() ++ getAllRhymeParts(hierarchy.values.toList)
//    HierarchicalIndexerResult(hierarchy, allWords)
//  }


  //  def removeSimilarRhymes(hierarchy: Map[String, ArtistNode]):Map[String, ArtistNode]={
  //    val list: List[RhymeLeaf] = allRhymes(hierarchy)
  //    hierarchy.foldLeft(Map[String, ArtistNode]()){(res, entry)=>{
  //      res(entry._1) = new ArtistNode(entry._2.children.map(album => {
  //        new AlbumNode(album.artist, album.title, album.year, album.children.map(song => {
  //          new SongNode(song.title, song.trackNo, song.rhymes.map(rhyme => rhyme))
  //        }))
  //      }))
  //    }}
  //  }
  //
  //  def allRhymes(hierarchy: Map[String, ArtistNode]):List[RhymeLeaf]={
  //    hierarchy.foldLeft(List[RhymeLeaf]()){(res, entry)=>{
  //      res ++ entry._2.children.map(album => {
  //        album.children.map(song => { song.rhymes })
  //      })
  //    }}
  //  }

  def applyToRhymeParts(artistFileName: String, song: SongNode, func: (String, RhymeLeaf) => RhymeLeaf): SongNode = {
    new SongNode(song.parent, song.title, song.trackNo, song.rhymes.map(song => func(artistFileName, song)));
  }

  def applyToRhymeParts(artistFileName: String, album: AlbumNode, func: (String, RhymeLeaf) => RhymeLeaf): AlbumNode = {
    new AlbumNode(album.parent, album.artist, album.title, album.year, album.children.map(s => applyToRhymeParts(artistFileName, s, func)))
  }

  def applyToRhymeParts(artistFileName: String, artist: ArtistNode, func: (String, RhymeLeaf) => RhymeLeaf): ArtistNode = {
    new ArtistNode(artist.name, artistFileName, artist.children.map(a => applyToRhymeParts(artistFileName, a, func)))
  }

  def applyToRhymeParts(hierarchy: Map[String, ArtistNode], func: (String, RhymeLeaf) => RhymeLeaf): Map[String, ArtistNode] = {
    hierarchy.foldLeft(Map[String, ArtistNode]()) {
      (res, entry) => {
        res(entry._1) = applyToRhymeParts(entry._1, entry._2, func);
      }
    }
  }

//  def getAllRhymeParts(artistNodes: List[ArtistNode]): List[String] = {
//    val listBuffer: ListBuffer[String] = new ListBuffer[String]()
//    artistNodes.foreach(artist => {
//      print(".")
//      artist.children.foreach(album => {
//        album.children.foreach(song => {
//          song.rhymes.foreach(rhyme => {
//            listBuffer.appendAll(rhyme.parts)
//          })
//        })
//      })
//    })
//    println()
//
//    listBuffer.toList.removeDuplicates
//  }

  def makeArtistHierarchy(artist: String): Map[String, ArtistNode] = {
    HierarchyBuilder.makeArtistsHierarchy(List(artist), OhhlaFiles.root)
    //new Map1(artist, makeArtistNode(OhhlaFiles.rawTargetLocation + "/" + artist))
  }

  //  def makeArtistAlbumsHierarchy(artistFolder: String, albums: List[String]): ArtistNode = {
  //    makeArtistNode(applyParentFolderNameToFolderNames(OhhlaFiles.rawTargetLocation + "/" + artistFolder, albums))
  //  }

  def applyParentFolderNameToFolderNames(parentFolderName: String, list: List[String]): List[String] = {
    list.map(parentFolderName + "/" + _)
  }

  object HierarchyBuilder {
    def makeArtistsHierarchy(artists: List[String], rootArtistFolder: String): Map[String, ArtistNode] = {
      return new HierarchyBuilder(rootArtistFolder).makeArtistsHierarchy(artists: List[String])
    }
  }

  def serializeResult(result: HierarchicalIndexerResult, file: String): Unit = {
    val objectOutputStream: ObjectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(file)))
    objectOutputStream.writeObject(result)
    objectOutputStream.flush()
    objectOutputStream.close()
  }

  def deserializeResult(file: String): HierarchicalIndexerResult = {
    val inputStream: ObjectInputStream = new ObjectInputStream(new FileInputStream(new File(file)))
    val readObject: Object = inputStream.readObject
    inputStream.close
    return readObject.asInstanceOf[HierarchicalIndexerResult]
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
    println("copying " + Constants.allRhymePartsFile + " to " + Constants.gaeAllRhymePartsFile)
  }


  //  private def getBestRhyme(songNode: SongNode): RhymeLeaf = {
  //    if (songNode.rhymes.length == 0) null
  //    else songNode.rhymes.sort((s1, s2) => s1.rating > s2.rating).head
  //  }

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

  /**for java compatibility */
  //  private def makeArtistHierarchySingle(artist: String): Map[String, ArtistNode] = {
  //    makeArtistsHierarchy(List(artist))
  //  }


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
}