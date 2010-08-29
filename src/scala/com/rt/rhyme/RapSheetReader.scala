package com.rt.rhyme

import java.io.{FileReader, FilenameFilter, File}
import java.lang.String
import com.rt.indexing.{RhymeLines, SongMetaData}
import com.rt.util.IO

case class RhymeRef(song: Song, verse: Verse, lines: List[Int]);
case class Song(verses: List[Verse]);
case class Verse(lines: List[String]);

// data about a song

object RapSheetReader{
  //private val rhymeFinder = new RhymeFinder(new CmuDictRhymeMap())
  private val rhymeFinder = new RhymeFinder(new RhymeZoneRhymeMap())

  def findRhymes(lines:List[String], songMetaData: SongMetaData): List[Rhyme] = {
    return new RapSheetReader(lines, songMetaData, rhymeFinder).findRhymes()
  }

  def findRhymes(file: String, songMetaData: SongMetaData): List[Rhyme] = {
    val lines:List[String] = IO.fileLines(new File(file)).map(_.trim)

    return new RapSheetReader(lines, songMetaData, rhymeFinder).findRhymes()
  }
}

class SongFileParser{

  def buildSong(lines: List[String]): Song = {
    val removedLineFeeds = lines.map(_.stripLineEnd)
    val removedChorus = removedLineFeeds.filter(!_.startsWith("[")).filter(!_.startsWith("("));
    val versesLists = splitOn(removedChorus, _ == "")
    val verses = versesLists.filter(containsInSubString(_, ":"))
    new Song(verses.map(Verse(_)))
  }

  def splitOn(lines: List[String], predicate: String => Boolean): List[List[String]] = {
    val res = lines.break(predicate)
    res._2 match {
      case List() => List(res._1)
      case _ => List(res._1) ::: splitOn(res._2.tail, predicate)
    }
  }

  /**
   * Is the given string present in any String in the given string list
   */
  private def containsInSubString(lines: List[String], st: String): Boolean = {
    !lines.forall(line => {
      line.contains(st)
    })
  }
}

/**
 * Prepares a list of lines to be parsed by a RhymeFinder
 *
 * Rules - an empty line symbolises a gap between verses
 */
class RapSheetReader(lines: List[String], songMetaData: SongMetaData, rhymeFinder:RhymeFinder){

  private val songFileParser:SongFileParser = new SongFileParser();

  def findRhymes(): List[Rhyme] = {
    findRhymes(songFileParser.buildSong(lines))
  }

  def findRhymes(song: Song): List[Rhyme] = {
    song.verses.foldLeft(List[Rhyme]()) {
      (rhymes, verse) => {
        val filtered:List[String] = removeBracketsAndContent(verse.lines.filter(isAllowedWord))
        rhymeFinder.findRhymesInLines(filtered) ::: rhymes
      }
    }
  }

  def removeBracketsAndContent(lines:List[String]):List[String]={
    lines.map(StringRhymeUtils.removeBrackets)
  }

  /** not sure why we need this, it is checking the whole line which sounds wrong. */
  def isAllowedWord(word: String): Boolean = {
    word.toCharArray.exists(_.isLetter)
  }

  def getRhymesFromStrings(strings: List[String], lineNumbers: List[List[Int]]): List[RhymeLines] = {
    lineNumbers.map(ln => new RhymeLines(this.songMetaData, getStrings(strings, ln)))
//    lineNumbers.foldLeft(List[RhymeLines]()) {
//      (result, ints) => {
//        result + new RhymeLines(this.songMetaData, getStrings(strings, ints))
//      }
//    }
  }

  def getStrings(strings:List[String], lineNumbers:List[Int]):List[String]={
    lineNumbers.foldLeft(List[String]()){(list, i) => {
      //list ++ strings(i)
      strings(i) :: list
    }}
  }

}

