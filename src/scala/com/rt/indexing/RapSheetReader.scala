package indexing

import collection.mutable.{HashMap, MultiMap}
import scala.collection.mutable.Set
import io.Source
import org.apache.commons.io.IOUtils
import java.io.{FileReader, FilenameFilter, File}
import java.lang.String


case class RhymeRef(song: Song, verse: Verse, lines: List[Int]);
case class WordRef(song: Song, verse: Verse, index: Int);
case class Song(verses: List[Verse]);
case class Verse(lines: List[String]);
case class RhymeLines(lines: List[String]);

/**
 * Rules - an empty line symbolises a gap between verses
 */
class RapSheetReader {
  //private val rhymeWords = List("GO  G OW1", "NO  N OW1", "BOYS  B OY1 Z", "NOISE  N OY1 Z")
  //private val rhymeMap = new OneThousandMostCommonWords().makeRhymeMap()
  private val rhymeMap = new CMUDict().makeRhymeMap();

  def readRapSheet(file: String): Map[String, List[RhymeRef]] = {
    val javaList = IOUtils.readLines(new FileReader(file))
    List.fromArray((javaList.toArray)).asInstanceOf[List[String]]
    findRhymes(buildSong(List.fromArray((javaList.toArray)).asInstanceOf[List[String]]))
  }

  /**
   * Returns a map of words to lines that rhyme them
   */
  def readRapSheetSimple(file: String): Map[String, List[RhymeLines]] = {
    val javaList = IOUtils.readLines(new FileReader(file))
    List.fromArray((javaList.toArray)).asInstanceOf[List[String]]
    findRhymesSimple(buildSong(List.fromArray((javaList.toArray)).asInstanceOf[List[String]]))
  }

  /**
   * Returns a map of words to lines that rhyme them
   */
  def findRhymesSimple(song: Song): Map[String, List[RhymeLines]] = {
    song.verses.foldLeft(Map[String, List[RhymeLines]]()) {
      (outerMap, verse) => {
        findRhymesInLines(verse.lines.filter(isAllowedWord)).elements.foldLeft(Map[String, List[RhymeLines]]()) {
          (innerMap, ints) => {
            innerMap(ints._1) = getRhymesFromVerse(verse, ints._2)
            //innerMap(r._1) = r._2.map(e => RhymeRef(song, verse, e))
          }
        }
      }
    }
  }

  def getRhymesFromVerse(verse: Verse, lines: List[List[Int]]): List[RhymeLines] = {
    lines.foldLeft(List[RhymeLines]()) {
      (result, ints) => {
        result + getLinesFromVerse(verse, ints)
      }
    }
    //    new RhymeLines(lines.foldLeft(List[String]{}){
    //      (l, i) =>{
    //        l + verse.lines(i)
    //      }
    //    })
  }

  def getLinesFromVerse(verse: Verse, lines: List[Int]): RhymeLines = {
    new RhymeLines(lines.foldLeft(List[String]()) {
      (l, i) => l + verse.lines(i)
    })
  }


  def buildSong(lines: List[String]): Song = {
    val removedLineFeeds = lines.map(_.stripLineEnd)
    val removedChorus = removedLineFeeds.filter(!_.startsWith("[")).filter(!_.startsWith("("));
    val versesLists = splitOn(removedChorus, _ == "")
    val verses = versesLists.filter(contains(_, ":"))
    new Song(verses.map(Verse(_)))
  }

  def isAllowedWord(word: String): boolean = {
    word.toCharArray.exists(_.isLetter)
  }

  def findRhymes(song: Song): Map[String, List[RhymeRef]] = {
    song.verses.foldLeft(Map[String, List[RhymeRef]]()) {
      (outerMap, verse) => {
        findRhymesInLines(verse.lines.filter(isAllowedWord)).elements.foldLeft(Map[String, List[RhymeRef]]()) {
          (innerMap, r) => {
            innerMap(r._1) = r._2.map(e => RhymeRef(song, verse, e))
          }
        }
      }
    }
  }

  /**
   *  returns a map of words to line numbers. Each word is mapped to where it appears
   * in the given list. It may be rhymed more than once.
   */
  def findRhymesInLines(lines: List[String]): Map[String, List[List[Int]]] = {
    //var resultIndex = scala.collection.mutable.Map[String, List[List[Int]]]()
    MapUtils.joinMaps(
      findMultiLineRhymes(lines) //,
      //findSingleLineRhymes(lines)
      )
  }


  def findMultiLineRhymes(lines: List[String]): Map[String, List[List[Int]]] = {
    var resultIndex = scala.collection.mutable.Map[String, List[List[Int]]]()

    val iter = lines.elements.counted
    var lastWordUppercase: String = null

    iter.foreach(line => {
      val wordUppercase = cleanWord(line.split(" ").last).toUpperCase

      if (lastWordUppercase == null) lastWordUppercase = wordUppercase
      else {
        //if ((word != lastWord) && rhymeMap.contains(word) && rhymeMap.contains(lastWord)) {
        if (compareWordsOnRhyme(wordUppercase, lastWordUppercase)) {
          resultIndex(wordUppercase.toLowerCase()) = resultIndex.getOrElse(wordUppercase, List[List[Int]]()) ::: List(List(iter.count - 1, iter.count))
          resultIndex(lastWordUppercase.toLowerCase()) = resultIndex.getOrElse(lastWordUppercase, List[List[Int]]()) ::: List(List(iter.count - 1, iter.count))
          lastWordUppercase = null
        } else {
          lastWordUppercase = wordUppercase
        }
      }
    })
    Map(resultIndex.toSeq: _*)
  }

  def findSingleLineRhymes(lines: List[String]): Map[String, List[List[Int]]] = {
    val iter = lines.elements.counted

    iter.foldLeft(Map[String, List[List[Int]]]()) {
      (map, line) => {
        val rhymes: List[String] = findRhymesInLine(line)
        if (rhymes.size != 0) println("found single rhymes: " + rhymes + " in " + line)
        MapUtils.addEntry(map, rhymes, iter.count)
      }
    }
  }


  /**
   * finds the rhymes in one line
   */
  def findRhymesInLine(line: String): List[String] = {
    val words = line.split(" ").map(cleanWord).toList

    // only need to return 'i' because the reverse will also rhyme
    for{i <- words; j <- words; if (compareWordsOnRhyme(i, j))} yield i
  }


  def doLastRhymeAndPrecedingLettersEqual(pairs: List[(String, String)]): boolean = {
    val equals = pairs.takeWhile(pair => (pair._1 == pair._2)).map(_._1)
    equals.size match {
      case 0 => false
      case _ => equals.findIndexOf(isRhymePart _) != -1
    }
  }

  def isRhymePart(part: String): boolean = {
    part.charAt(part.length - 1) match {
      case '0' => true
      case '1' => true;
      case '2' => true;
      case _ => false
    }
  }

  /**
   * Given 2 lines find out if they rhyme by looking at
   * the last word in each line, Words must be in uppercase
   */
  def compareWordsOnRhyme(one: String, two: String): boolean = {
    val oneUpper = one.toUpperCase()
    val twoUpper = two.toUpperCase()
    if ((oneUpper != twoUpper) && (rhymeMap.contains(oneUpper) && rhymeMap.contains(twoUpper))) {
      val it1 = rhymeMap(oneUpper).split(" ").reverse.elements
      val it2 = rhymeMap(twoUpper).split(" ").reverse.elements

      doLastRhymeAndPrecedingLettersEqual(it1.zip(it2).toList)
    } else {
      false
    }
  }

  def compareWordsOnRhyme_OLD(in: (String, String)): boolean = {
    // in.
    if (rhymeMap.contains(in._1) && rhymeMap.contains(in._2)) {
      val it1 = rhymeMap(in._1).split(" ").reverse.elements
      val it2 = rhymeMap(in._2).split(" ").reverse.elements

      doLastRhymeAndPrecedingLettersEqual(it1.zip(it2).toList)
    } else {
      false
    }
  }

  def cleanWord(word: String): String = {
    def option = "[\\w']+".r.findFirstIn(word)
    option match {
      case None => ""
      case _ => option.get
    }
  }

  def contains(lines: List[String], st: String): Boolean = {
    !lines.forall(line => {
      line.contains(st)
    })
  }

  def buildIndexForAlbum(folder: String): Map[String, List[RhymeRef]] = {
    val albumFiles = new File(folder).listFiles(new FilenameFilter() {
      def accept(dir: File, name: String): Boolean = {name.endsWith("txt")}
    })

    var res: collection.mutable.HashMap[String, List[RhymeRef]] = new collection.mutable.HashMap[String, List[RhymeRef]]()
    val reader = new RapSheetReader()
    albumFiles.foreach(f => {
      reader.readRapSheet(f.getAbsolutePath()).foreach(entry => {
        res += entry._1 -> (res.getOrElse(entry._1, List[RhymeRef]()) ::: entry._2.toList)
      })
    })
    Map(res.toSeq: _*)
  }

  def main(args: Array[String]): Unit = {
    //read("C:\\data\\projects\\rhyme-0.9\\wtc1_snIPez\\01-bring_da_ruckus.txt");
    //def rhymeMap = buildRhymeMap();
    //println("findRhymes: " + rhymeMap)
    //readRapSheet("""C:\data\projects\rhyme-0.9\bb\fight.txt""")
    val res = buildIndexForAlbum("""C:\data\projects\rhyme-0.9\olhha\Beastie Boys\Licensed_to_Ill""")
    println("res is " + res)
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

