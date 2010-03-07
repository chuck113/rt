package indexing

import collection.immutable.Map
import java.lang.String
import org.apache.commons.io.IOUtils
import java.io.{FileOutputStream, File}
import java.util.Arrays

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 09-Feb-2010
 * Time: 18:09:36
 * To change this template use File | Settings | File Templates.
 */

object IndexerObj{
  def main(args: Array[String])={
    val index: Map[String, List[RhymeLines]] = new Indexer().indexAll
    println ("index length is "+index.size)
    buildFileIndex(index)
  }

  def buildFileIndex(index: Map[String, List[RhymeLines]]) = {
    val rootIndexFolder:String = """C:\data\projects\rapAttack\rapAttack""" +File.separator+"indexes";
    new File(rootIndexFolder).mkdirs()
    def wordIndexFile:File = new File(rootIndexFolder+File.separator+"wordIndex.txt");
    def songIndexFile:File = new File(rootIndexFolder+File.separator+"songIndex.txt");
    wordIndexFile.createNewFile()
    songIndexFile.createNewFile()

    val lines:List[String] = index.foldLeft(List[String]()){
      (list, e) => {
        list + (e._1 +":("+e._2+")")
      }
    }

    //val javaList:java.util.ArrayList[String]  = new java.util.ArrayList[String](lines);
    //Arrays.asList(lines.toArray : _*)
    IOUtils.writeLines(Arrays.asList(lines.toArray : _*) , "\n", new FileOutputStream(wordIndexFile))
  }
}