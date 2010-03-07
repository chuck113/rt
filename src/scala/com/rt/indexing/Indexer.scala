package indexing

import org.apache.commons.io.FileUtils
import collection.mutable.MultiMap
import ohhla.{AlbumMetaData, OhhlaConfig}
import xml.{XML, Elem}
import java.io.{FileInputStream, FileFilter, File}

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 09-Feb-2010
 * Time: 18:06:08
 * To change this template use File | Settings | File Templates.
 */

case class TrackMetaData(title: String, number: Int, albumData: AlbumMetaData)

class Indexer {

  val rapSheetReader = new RapSheetReader()

  def dirFilter = new FileFilter {
    def accept(f: File) = f.isDirectory
  }

  def textFileFilter = new FileFilter {
    def accept(f: File) = f.getName.endsWith(".txt")
  }

  def textFilesInFolder(dir: File):List[File]={
    dir.listFiles(textFileFilter).toList
  }

  def foldersInDir(dir : File):List[File]={
    dir.listFiles(dirFilter).toList 
  }

  def filesInDir(dir : File):List[File]={
    dir.listFiles().toList 
  }

//  object FriendFeed {
//  import java.net.{URLConnection, URL}
//  import scala.xml._
//  def friendFeed():Elem = {
//    val url = new URL("http://friendfeed.com/api/feed/public?format=xml&num=100")
//    val conn = url.openConnection
//    XML.load(conn.getInputStream)
//  }
//}

  private def toElem(metaDataPath: String):Elem={
    XML.load(new FileInputStream(metaDataPath))
  }

  private def buildAlbumMetaData(metaData: Elem):AlbumMetaData = {
    println("elem is "+metaData)
    new AlbumMetaData(
      (metaData \\ "@artist").text,
      (metaData \\ "@title").text,
      (metaData \\ "@year").text
    )
  }

  def indexAll():Map[String, List[RhymeLines]] = {
    val all = foldersInDir(new File(OhhlaConfig.rawTargetLocation))
    var indexMap: collection.mutable.HashMap[String, List[RhymeLines]] = new collection.mutable.HashMap[String, List[RhymeLines]]()
    println("will index: "+all.toList)
    //val dir = all.head
    all.foreach(dir => {
      val albumFolders = foldersInDir(dir);
      albumFolders.foreach(albumFolder => {
        println("albumFolder = " + albumFolder)
        val data: AlbumMetaData = buildAlbumMetaData(toElem(albumFolder+"/md.xml"))
        println("AlbumMetaData = " + data)
        //val songMappingFile = albumFolder+"/md.xml"
        textFilesInFolder(albumFolder).foreach(f => {
          println(f)
          val songIndex: Map[String, List[RhymeLines]] = rapSheetReader.readRapSheetSimple(f.getAbsolutePath());
          songIndex.foreach(entry => {
             indexMap += entry._1 -> (indexMap.getOrElse(entry._1, List[RhymeLines]()) ::: entry._2.toList)
          })
        })
      })
    })

    //println("index has is "+indexMap.size+" words")
    //indexMap.foreach(entry => println("word is "+ entry._1+", has "+ entry._2.size+" entries"))
    val list: List[(String, List[RhymeLines])] = indexMap.elements.toList.sort(wordScoreSorter)
    list.foreach(entry => println("word is "+ entry._1+", has "+ entry._2.size+" entries ("+wordScore(entry._1, entry._2.size)+")"))
    Map[String,List[RhymeLines]](indexMap.toList:_*)
  }

  private def wordScoreSorter(first: (String, List[RhymeLines]), second: (String, List[RhymeLines])): Boolean = {
     wordScore(first._1, first._2.size) < wordScore(second._1, second._2.size)
  }

  private def wordScore(word:String, entries: Int): Int ={
    (word.length * word.length) * entries
  }
}