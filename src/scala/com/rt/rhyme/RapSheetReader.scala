package com.rt.rhyme

import collection.mutable.{HashMap, MultiMap}
import scala.collection.mutable.Set
import io.Source
import org.apache.commons.io.IOUtils
import java.io.{FileReader, FilenameFilter, File}
import java.lang.String
import util.IO
import com.rt.rhyme.RhymeFinder
import com.rt.indexing.{RhymeLines, SongMetaData}
import com.rt.util.IO

case class RhymeRef(song: Song, verse: Verse, lines: List[Int]);
case class Song(verses: List[Verse]);
case class Verse(lines: List[String]);

// data about a song
//case class SongMetaData(title: String, artist:String, year:Int, album:String, track:Int);


object RapSheetReader{
  private val rhymeFinder = new RhymeFinder(new RhymeMap())

  def findRhymes(lines:List[String], songMetaData: SongMetaData): List[Rhyme] = {
    return new RapSheetReader(lines, songMetaData, rhymeFinder).findRhymes()
  }

  def findRhymes(file: String, songMetaData: SongMetaData): List[Rhyme] = {
    val lines:List[String] = IO.fileLines(new File(file)).map(_.trim)

    return new RapSheetReader(lines, songMetaData, rhymeFinder).findRhymes()
  }
}

/**
 * Prepares a list of lines to be parsed by a RhymeFinder
 *
 * Rules - an empty line symbolises a gap between verses
 */
class RapSheetReader(lines: List[String], songMetaData: SongMetaData, rhymeFinder:RhymeFinder){

  def findRhymes(): List[Rhyme] = {
    findRhymes(buildSong(lines))
  }

  def findRhymes(song: Song): List[Rhyme] = {
    song.verses.foldLeft(List[Rhyme]()) {
      (rhymes, verse) => {
        val filtered:List[String] = verse.lines.filter(isAllowedWord)
        rhymeFinder.findRhymesInLines(filtered) ::: rhymes
      }
    }
  }

//  private def convert(from:List[Rhyme]):List[RhymeLines]={
//    from.foldLeft(List[RhymeLines]()){(list, rhyme)=>{
//      list :: new RhymeLines(this.songMetaData,rhyme.parts);
//    }}
//  }

  def getRhymesFromStrings(strings: List[String], lineNumbers: List[List[Int]]): List[RhymeLines] = {
    lineNumbers.foldLeft(List[RhymeLines]()) {
      (result, ints) => {
        result + new RhymeLines(this.songMetaData, getStrings(strings, ints))
      }
    }
  }

  def getStrings(strings:List[String], lineNumbers:List[Int]):List[String]={
    lineNumbers.foldLeft(List[String]()){(list, i) => {
      list + strings(i)
    }}
  }

  def getRhymesFromVerse(verse: Verse, lines: List[List[Int]]): List[RhymeLines] = {
    lines.foldLeft(List[RhymeLines]()) {
      (result, ints) => {
        result + getLinesFromVerse(verse, ints)
      }
    }
  }

  def getLinesFromVerse(verse: Verse, lines: List[Int]): RhymeLines = {
    new RhymeLines(this.songMetaData, lines.foldLeft(List[String]()) {
      (l, i) => l + verse.lines(i)
    })
  }


  def buildSong(lines: List[String]): Song = {
    val removedLineFeeds = lines.map(_.stripLineEnd)
    val removedChorus = removedLineFeeds.filter(!_.startsWith("[")).filter(!_.startsWith("("));
    val versesLists = splitOn(removedChorus, _ == "")
    val verses = versesLists.filter(rhymeFinder.containsInSubString(_, ":"))
    new Song(verses.map(Verse(_)))
  }

  def isAllowedWord(word: String): boolean = {
    word.toCharArray.exists(_.isLetter)
  }

//  def findRhymesOld(song: Song): Map[String, List[RhymeRef]] = {
//    song.verses.foldLeft(Map[String, List[RhymeRef]]()) {
//      (outerMap, verse) => {
//        rhymeFinder.findRhymesInLinesOld(verse.lines.filter(isAllowedWord)).elements.foldLeft(Map[String, List[RhymeRef]]()) {
//          (innerMap, r) => {
//            innerMap(r._1) = r._2.map(e => RhymeRef(song, verse, e))
//          }
//        }
//      }
//    }
//  }

  def main(args: Array[String]): Unit = {
    //read("C:\\data\\projects\\rhyme-0.9\\wtc1_snIPez\\01-bring_da_ruckus.txt");
    //def rhymeMap = buildRhymeMap();
    //println("findRhymesOld: " + rhymeMap)
    //readRapSheet("""C:\data\projects\rhyme-0.9\bb\fight.txt""")
    //val res = buildIndexForAlbum("""C:\data\projects\rhyme-0.9\olhha\Beastie Boys\Licensed_to_Ill""")
    //println("res is " + res)
    //val ref: RhymeRef = index("DAY").toList.head
    //println("rhyme for play is "+ref)
    //println(ref.verse.lines(ref.lines._1)+" / "+ref.verse.lines(ref.lines._2))

  }

  def splitOn(lines: List[String], predicate: String => Boolean): List[List[String]] = {
    val res = lines.break(predicate)
    res._2 match {
      case List() => List(res._1)
      case _ => List(res._1) ::: splitOn(res._2.tail, predicate)
    }
  }

}

