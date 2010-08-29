package com.rt.ohhla

import java.io.File
import com.rt.util.{Folders, IO}


object OhhlaConfig{
  val ohhlaUrl = "http://ohhla.com/"  
  val rawTargetLocation = """C:\data\projects\rapAttack\rapAttack\olhha"""
  val testRoot = """C:\data\projects\rapAttack\rapAttack\olhha-testdata"""
  val ohhlaLocalSiteStart = "ohhla-site/all.html"
  val ohhlaLocalSiteRoot = "ohhla-site"
  val ohhlaLocalSiteAll: List[String] = List("all.html", "all_two.html", "all_three.html", "all_four.html", "all_five.html")

  def allArtistFolderNames():List[String]={
    Folders.folderNamesInFolder(rawTargetLocation)
  }

  def allArtistFolderNames(folderLocation:String):List[String]={
    Folders.folderNamesInFolder(folderLocation)
  }

  def albumFolderExists(artistFolderName:String, albumFolderName:String):Boolean={
    new File(rawTargetLocation+"/"+artistFolderName+"/"+albumFolderName).exists
  }

  def albumFolder(artistAlbumFolderName:String):Option[String]={
    folderExists(artistAlbumFolderName)
//    new File(rawTargetLocation+"/"+artistAlbumFolderName).exists match{
//      case true => Some(rawTargetLocation+"/"+artistAlbumFolderName)
//      case false => None
//    }
  }

  def albumNamesForArist(artistName:String):List[String]={
    new File(rawTargetLocation+"/"+artistName).listFiles.toList.map(_.getName)      
  }

  private def folderExists(folder:String):Option[String]={
    new File(rawTargetLocation+"/"+folder).exists match{
      case true => Some(rawTargetLocation+"/"+folder)
      case false => None
    }
  }

  def artistFolder(artistFolderName:String):Option[String]={
    folderExists(artistFolderName)
  }

//  def allArtistFoldersToFiles():Map[String, File]={
//    val names:List[String] = Folders.foldersInDir(rawTargetLocation).map(_.getName);
//    ScalaConversions.toMap(Folders.foldersInDir(rawTargetLocation), ((f:File) => f.getName))
//  }

  def allArtistFolders():List[String]={
    Folders.foldersInDir(rawTargetLocation).map(_.getAbsolutePath)
  }

  def allTransformedArtistNamesToFiles():Map[String,File]={
    Folders.folderNamesInFolderToFiles(rawTargetLocation, fileNameTransformer)
  }

  private def fileNameTransformer(s:String):String={
    s.replace("_", " ")
  }

  /**
   * The Ohhla list if artists is spread accros 5 pages, this method combines those
   * files into one list of stirngs
   */
  def allOhhlaIndexes():List[String]={
    val cpResources = OhhlaConfig.ohhlaLocalSiteAll.map(l => IO.readLinesFromClassPathFile(OhhlaConfig.ohhlaLocalSiteRoot + "/" + l))
    cpResources.foldLeft(List[String]()) {(list, lines) => {list ::: lines}}
  }

  def urlForArtist(artistName: String): Option[String] = {
    def res = allOhhlaIndexes.filter(_.contains(">" + artistName + "<"));
    res.length match {
      case 0 => None
      case 1 => Some(ParsingUtils.betweenFirstAndLastQuotes(res.head))
    }
  }
}