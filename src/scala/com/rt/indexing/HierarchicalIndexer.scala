package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import com.rt.dto.DataMapper
import persistence.{AlbumTrack, AlbumMetaData, ArtistAlbums, Constants}
import org.apache.commons.io.FileUtils
import java.lang.String
import collection.immutable.{Map, Set => MutableSet}
import collection.mutable.{MultiMap, ListBuffer}
import com.rt.rhyme.{StringRhymeUtils, Rhyme, RapSheetReader}
import java.io._
import com.rt.util.{Logger, NameMapper}
import collection.JavaConversions._
import reflect.BeanProperty

// This contains all of the items the iphone will need and are added after the
// object is created which is pretty but simple for the moment
case class RhymeLeaf(word: String, lines: List[String], parts: List[String], rating: Int, suitability: Int, artistScore: Int) extends Serializable {
  def this(word: String, lines: List[String], parts: List[String]) = this (word, lines, parts, 0, 0, 0)

  def linesJList(): java.util.List[String] = {
    asList(lines.toSeq)
  }

  def partsJList(): java.util.List[String] = {
    asList(parts.toSeq)
  }
}

case class SongNode(title: String, trackNo: Int, rhymes: List[RhymeLeaf]) extends Serializable {
  def rhymesJList(): java.util.List[RhymeLeaf] = {
    asList(rhymes.toSeq)
  }
}


case class AlbumNode(artist: String, title: String, year: Int, children: List[SongNode]) extends Serializable {
  def childrenJList(): java.util.List[SongNode] = {
    asList(children.toSeq)
  }
}

case class ArtistNode(children: List[AlbumNode]) extends Serializable {
  def childrenJList(): java.util.List[AlbumNode] = {
    asList(children.toSeq)
  }
}

case class HierarchicalIndexerResult(index: Map[String, ArtistNode], foundWords: Set[String]) extends Serializable {
  def bestRhymes(): List[RhymeLeaf] = {
    val sorted = rhymeList(index.values.toList).sort((a, b) => a.rating > b.rating)
    //sorted.foreach(println)
    sorted
  }

  def rhymeList(artistNodes: List[ArtistNode]): List[RhymeLeaf] = {
    val rhymeBuffer: ListBuffer[RhymeLeaf] = new ListBuffer[RhymeLeaf]();
    artistNodes.foreach(artist => {
      artist.children.foreach(album => {
        album.children.foreach(song => {
          song.rhymes.foreach(rhyme => {
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

  class ProgressPrinter {
    var count = 0
    val newLineCount = 50
    val tenChar = "X"

    def done() {println()}

    def inc() {
      if (count == 0) {
        print(".")
      } else if (count % newLineCount == 0) {
        println(".(" + newLineCount + ")")
      } else if (count % 10 == 0) {
        println(tenChar)
      } else {
        print(".")
      }
    }
  }


  def makeArtistHierarchyWithAllWords(artists: List[String], rootFolder: String): HierarchicalIndexerResult = {
    val hierarchy: Map[String, ArtistNode] = applyToRhymeParts(HierarchyBuilder.makeArtistsHierarchy(artists, rootFolder), RhymeScoreCalculator.calculate)
    val allWords: Set[String] = Set() ++ getAllRhymeParts(hierarchy.values.toList)
    HierarchicalIndexerResult(hierarchy, allWords)
  }

  def makeArtistHierarchyWithAllWords(artists: List[String]): HierarchicalIndexerResult = {
    val hierarchy: Map[String, ArtistNode] = applyToRhymeParts(HierarchyBuilder.makeArtistsHierarchy(artists, OhhlaConfig.rawTargetLocation), RhymeScoreCalculator.calculate)
    val allWords: Set[String] = Set() ++ getAllRhymeParts(hierarchy.values.toList)
    HierarchicalIndexerResult(hierarchy, allWords)
  }


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
    new SongNode(song.title, song.trackNo, song.rhymes.map(song => func(artistFileName, song)));
  }

  def applyToRhymeParts(artistFileName: String, album: AlbumNode, func: (String, RhymeLeaf) => RhymeLeaf): AlbumNode = {
    new AlbumNode(album.artist, album.title, album.year, album.children.map(s => applyToRhymeParts(artistFileName, s, func)))
  }

  def applyToRhymeParts(artistFileName: String, artist: ArtistNode, func: (String, RhymeLeaf) => RhymeLeaf): ArtistNode = {
    new ArtistNode(artist.children.map(a => applyToRhymeParts(artistFileName, a, func)))
  }

  def applyToRhymeParts(hierarchy: Map[String, ArtistNode], func: (String, RhymeLeaf) => RhymeLeaf): Map[String, ArtistNode] = {
    hierarchy.foldLeft(Map[String, ArtistNode]()) {
      (res, entry) => {
        res(entry._1) = applyToRhymeParts(entry._1, entry._2, func);
      }
    }
  }

  def getAllRhymeParts(artistNodes: List[ArtistNode]): List[String] = {
    val listBuffer: ListBuffer[String] = new ListBuffer[String]()
    artistNodes.foreach(artist => {
      print(".")
      artist.children.foreach(album => {
        album.children.foreach(song => {
          song.rhymes.foreach(rhyme => {
            listBuffer.appendAll(rhyme.parts)
          })
        })
      })
    })
    println()

    listBuffer.toList.removeDuplicates
  }

  def makeArtistHierarchy(artist: String): Map[String, ArtistNode] = {
    HierarchyBuilder.makeArtistsHierarchy(List(artist), OhhlaConfig.rawTargetLocation)
    //new Map1(artist, makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist))
  }

  //  def makeArtistAlbumsHierarchy(artistFolder: String, albums: List[String]): ArtistNode = {
  //    makeArtistNode(applyParentFolderNameToFolderNames(OhhlaConfig.rawTargetLocation + "/" + artistFolder, albums))
  //  }

  def applyParentFolderNameToFolderNames(parentFolderName: String, list: List[String]): List[String] = {
    list.map(parentFolderName + "/" + _)
  }

  private object HierarchyBuilder {
    def makeArtistsHierarchy(artists: List[String], rootArtistFolder: String): Map[String, ArtistNode] = {
      return new HierarchyBuilder(rootArtistFolder).makeArtistsHierarchy(artists: List[String])
    }
  }

  class HierarchyBuilder(rootFolder: String) {
    private def checkFoldersExist(artists: List[String]) {
      artists.foreach(artist => {
        if (!new File(rootFolder + "/" + artist).exists) {
          throw new ArtistFolderNotFoundException("for artist '" + artist + "' in " + rootFolder)
        }
      })
    }

    def makeArtistsHierarchy(artists: List[String]): Map[String, ArtistNode] = {
      checkFoldersExist(artists)
      val printer: ProgressPrinter = new ProgressPrinter()
      Logger.progress("building artist heirarchy")
      artists.foldLeft(Map[String, ArtistNode]()) {
        (map, artist) => {
          printer.inc
          map(artist) = makeArtistNode(rootFolder + "/" + artist)
        }
      }
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

    private def makeAlbumNode(albumFolder: String): AlbumNode = {
      val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
      val songNodes = md.tracks.foldLeft(List[SongNode]()) {
        (list, track) => {
          val file = albumFolder + "/" + track.number + ".txt"
          makeSongNode(file, makeSongMetaData(md, track)) :: list
        }
      }
      AlbumNode(md.artist, md.title, md.year, songNodes)
    }

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

    def rhymeExists(rhyme: Rhyme, knownRhymes: List[Rhyme]): Boolean = {
      if (knownRhymes.size == 0) {
        return false
      } else {
        knownRhymes.forall(known => StringRhymeUtils.areLinesSimilar(rhyme.lines, known.lines))
      }
    }

    def removeSimilarRhymes(newRhymes: List[Rhyme], knownRhymes: List[Rhyme]): List[Rhyme] = {
      newRhymes.filter(r => {
        !rhymeExists(r, knownRhymes)
      })
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

  //TODO this is in Indxer too, put into abstract class
  private def makeSongMetaData(album: AlbumMetaData, track: AlbumTrack): SongMetaData = {
    new SongMetaData(track.title, album.artist, album.year, album.title, track.number)
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