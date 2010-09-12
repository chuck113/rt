package com.rt.indexing

import java.lang.String
import com.rt.rhyme.{StringRhymeUtils, Rhyme, RapSheetReader}
import java.io._
import com.rt.util.Logger
import collection.immutable.{List, Map, Set => MutableSet}
import com.rt.ohhla.persistence.{AlbumTrack, Album, AlbumMetaData, ArtistAlbums}

class HierarchyBuilder(rootFolder: String) {

  private def checkFoldersExist(artists: List[String]) {
    artists.foreach(artist => {
      if (!new File(rootFolder + "/" + artist).exists) {
        throw new ArtistFolderNotFoundException("for artist '" + artist + "' in " + rootFolder)
      }
    })
  }

  def makeArtistsHierarchy(): Map[String, ArtistNode] = {
    makeArtistsHierarchy(new File(rootFolder).list.toList)
  }

  def makeArtistsHierarchy(artists: List[String]): Map[String, ArtistNode] = {
    checkFoldersExist(artists)
    val printer: ProgressPrinter = new ProgressPrinter()
    Logger.progress("building artist heirarchy")
    artists.foldLeft(Map[String, ArtistNode]()) {
      (map, artist) => {
        printer.inc
        map(artist) = makeArtistNode(artist)
      }
    }
  }

  private def makeArtistNode(artistFolder: String): ArtistNode = {
    val folder: String = rootFolder + "/" + artistFolder
    val artist:ArtistAlbums = ArtistAlbums.fromFolder(folder)

    val partialFactoryMethods: List[(ArtistNode) => AlbumNode] = artist.albums.map(album => makeAlbumNode(makeAlbumFolder(folder, album)))

    ArtistNode(artist.artist, artistFolder).addChildren(partialFactoryMethods)
  }

  private def makeAlbumNode(albumFolder: String): (ArtistNode => AlbumNode) = {
    val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)

    val partialFactoryMethods: List[(AlbumNode) => SongNode] = md.tracks.map(buildSongNode(albumFolder, _, md))
    AlbumNode(_)(md.artist, md.title, md.year).addChildren(partialFactoryMethods)
  }


  private def makeSongNode(trackFile: String, song: SongMetaData): (AlbumNode => SongNode) = {
    val rhymes: List[Rhyme] = indexTrack(trackFile, song)
    val partialFactoryMethods: List[(SongNode) => RhymeLeaf] = rhymes.map(r => buildRhymeLeaves(r)).flatten
    SongNode(_)(song.title, song.track).addLeaves(partialFactoryMethods)
  }

  private def buildRhymeLeaves(rhyme: Rhyme): List[(SongNode => RhymeLeaf)] = {
    rhyme.parts.map(part => {
      def partialFactoryMethod:(SongNode => RhymeLeaf) = RhymeLeaf(_)(part, rhyme.lines, rhyme.parts)
      partialFactoryMethod
    })
  }

  private def indexTrack(trackFile: String, song: SongMetaData): List[Rhyme] = {
    RapSheetReader.findRhymes(trackFile, song);
  }

  private def makeAlbumFolder(artistFolder: String, album: Album): String = {
    artistFolder + "/" + album.fileInfo.fileName
  }

  private def makeSongFile(albumFolder: String, track: AlbumTrack): String = {
    albumFolder + "/" + track.number + ".txt"
  }

  private def buildSongNode(albumFolder: String, track: AlbumTrack, md: AlbumMetaData): (AlbumNode => SongNode) = {
    makeSongNode(makeSongFile(albumFolder, track), makeSongMetaData(md, track))
  }

  /**
   * Used for testing when we find an error in a rhyme in a song
   */
  def makeAlbumNodeForOneSong(albumFolder: String, trackNumber: Int): AlbumNode = {
    val md: AlbumMetaData = AlbumMetaData.fromFolder(albumFolder)
    val file = albumFolder + "/" + trackNumber + ".txt"
    val songNode: (AlbumNode) => SongNode = makeSongNode(file, makeSongMetaData(md, AlbumTrack("title", trackNumber, "url")))
    AlbumNode(null)(md.artist, md.title, md.year).addChildren(List(songNode))
  }
      
  private def makeSongMetaData(album: AlbumMetaData, track: AlbumTrack): SongMetaData = {
    new SongMetaData(track.title, album.artist, album.year, album.title, track.number)
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
}