package com.rt.indexing

import collection.immutable.Map
import java.lang.String
import org.apache.commons.io.IOUtils
import java.util.Arrays
import com.rt.ohhla.OhhlaConfig
import com.rt.dto.DataMapper
import java.io.{FileFilter, FileInputStream, FileOutputStream, File}
import persistence.Constants

object IndexerObj{
  val indexer = new Indexer()

  def main(args: Array[String]) = {
    val toIndex = List(
      "Beastie_Boys", "Jay-Z", "Ice_Cube", "Notorious_B_I_G_", "Roots,_The", "Snoop_Dogg", "Wu-Tang_Clan",
      "Dr__Dre", "Fat_Joe", "Ghostface_Killah", "Guru", "Kool_G_Rap_and_DJ_Polo",
      "Method_Man", "Missy_'Misdemeanor'_Elliott", "Nas", "Mobb_Deep", "Redman", "Run-D_M_C_", "Talib_Kweli")

    //val toIndex = getAllArtistList(OhhlaConfig.rawTargetLocation)
    //val toIndex = List("Nas")
    //println(split(List(1, 2, 3, 4, 5, 6, 7, 8, 9),2));
    //val artistNode: ArtistNode = indexer.makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + "Nas")
    //artistNode.children.foreach(album => println(album.children))
    val artistNodes:Map[String, ArtistNode] = makeArtistHierarchy(toIndex)
    //artistNodes.foreach(artist => (println("album = "+artist)))
    serializeHierarcyInBatches(artistNodes, 3)
    //val index: Map[String, List[RhymeLines]] = indexer.indexArtists(OhhlaConfig.rawTargetLocation, toIndex)

    //val scoringIndex = indexer.buildScoringIndex(index)
    //serializeIndex(index)
    //val deserializedIndex = deserializeIndex()
    //println ("index length is "+deserializedIndex.size)
    null
  }

  def split[T](l:List[T], size:Int):List[List[T]]={
    var res:List[List[T]] = List[List[T]]()
    //var res:LinkedList[List[T]] = new LinkedList[List[T]]()
    //println("size is "+(l.length /size)+" length: "+l.length+", size: "+size)
    for(index <- 0 to (l.length /size)) {
      //println("index is "+index)
      val from:Int = index * size
      val to:Int = from + size
      //println("slice is "+l.slice(from, to))
      res += l.slice(from, to)
    }

    res
  }

  def serializeHierarcyInBatches(hierarchy:Map[String, ArtistNode], batches:Int):Unit ={
    //val list: List[List[ArtistNode]] = split(hierarchy, batches)
    //val pair:(List[ArtistNode], List[ArtistNode]) = hierarchy.splitAt(batches);
    //serializeHierarcy(pair._1)
    val mapper = new DataMapper()
    hierarchy.foreach(entry =>{
      val file = Constants.serialisedIndexHierarchyFolder("-"+entry._1)
      mapper.write(entry._2, new FileOutputStream(file))
    })
  }

  def serializeHierarcy(hierarchy:Map[String, ArtistNode], toFile:String):Unit ={
    new DataMapper().write(hierarchy, new FileOutputStream(toFile));
  }

  /** for java compatibility */
  def makeArtistHierarchySingle(artist:String):Map[String, ArtistNode]={
    makeArtistHierarchy(List(artist))    
  }

  def makeArtistHierarchy(artists:List[String]):Map[String, ArtistNode]={
    artists.foldLeft(Map[String, ArtistNode]()){(map,artist)=>
      map(artist) = indexer.makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist)
    }
    //artists.map(artist => indexer.makeArtistNode(OhhlaConfig.rawTargetLocation + "/" + artist))      
  }

  def getAllArtistList(rootFolder:String):List[String] ={
    foldersInDir(new File(rootFolder)).foldLeft(List[String]()){(list, folder)=>{
      list + folder.getName
    }}
  }

  def foldersInDir(dir: File): List[File] = {
    dir.listFiles(dirFilter).toList
  }

  def dirFilter = new FileFilter {
    def accept(f: File) = f.isDirectory
  }

  def serializeIndex(index:Map[String, List[RhymeLines]])= {
    new File(Constants.serializedIndexesFolder).mkdirs()
    def wordIndexFile:File = new File(Constants.serializedIndexesFolder);
    val jMap = MapUtils.toJavaMapOfLists(index)
    new DataMapper().write(jMap, new FileOutputStream(wordIndexFile));
  }

  def deserializeIndex():Map[String, List[RhymeLines]]={
    def wordIndexFile:File = new File(Constants.serializedIndexesFolder);
    MapUtils.toScalaMap(new DataMapper().read(new FileInputStream(wordIndexFile)))
  }

//  def songIdBuilder(song:SongMetaData):String ={
//
//  }

  def buildSongIndex(index: Map[String, List[RhymeLines]]) = {

  }

//  def buildAlbumIndex(index: Map[String, List[RhymeLines]]) = {
//    index.elements.foreach(entry => {
//      entry._2.foreach(rhymeLines => {
//        val album = rhymeLines.song.album
//      })
//    })
//  }

  def buildFileIndex(index: Map[String, List[RhymeLines]]) = {
    val rootIndexFolder:String = """C:\data\projects\rapAttack\rapAttack""" +File.separator+"indexes";
    new File(rootIndexFolder).mkdirs()
    def wordIndexFile:File = new File(rootIndexFolder+"/wordIndex.txt");
    def songIndexFile:File = new File(rootIndexFolder+"/songIndex.txt");
    wordIndexFile.createNewFile()
    songIndexFile.createNewFile()

    val lines:List[String] = index.foldLeft(List[String]()){
      (list, e) => { list + (e._1 +":("+e._2+")")}
    }

    //val javaList:java.util.ArrayList[String]  = new java.util.ArrayList[String](lines);
    //Arrays.asList(lines.toArray : _*)
    IOUtils.writeLines(Arrays.asList(lines.toArray : _*) , "\n", new FileOutputStream(wordIndexFile))
  }
}