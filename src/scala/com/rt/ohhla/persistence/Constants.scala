package com.rt.ohhla.persistence

import java.io.File


object Constants{
  val dot = "."
  val sep = "/"
  val artistMetaDataFileName:String = "artist.json"
  val albumMetaDataFileName:String = "album.json"

  val rootDataFolder ="""C:\data\projects\rapAttack\rapAttack"""
  val gaeProjectFolder ="""C:\data\projects\rapAttack\rapAttackGAE"""
  val tmpFolder = System.getProperty("java.io.tmpdir")+sep+"rhymeTimeTmp"
  val serializedIndexesFolder = rootDataFolder+File.separator+"indexes";


  val allRhymePartsFileName = "allParts.ser"
  val allRhymePartsFile = tmpFolder+sep+allRhymePartsFileName

  val gaeResourceFolderName = "resources"
  val serializedFileExtension = "ser"
  val serializedIndexesFileName = "index"
  val serializedIndexesZipFolderName = "index"
  val serialisedIndexHierarchyFileName = "hierarcy-index"
  //val serializedIndexesFolder = rootDataFolder+sep+serializedIndexesFileName+dot+serializedFileExtension
  //val serialisedIndexHierarchyFolder = indexesFolder+sep+serialisedIndexHierarchyFileName+dot+serializedFileExtension
  val indexZipName = "index.zip"

  val tmpIndexFile = new File(tmpFolder+sep+serializedIndexesFileName)

  val serialisedIndexHierarchyZipFile = tmpFolder+sep+indexZipName
  val serializedIndexesZipFolder = tmpFolder+sep+serializedIndexesZipFolderName
  val gaeResourceFolder = gaeProjectFolder+sep+gaeResourceFolderName
  val gaeIndexFolder = gaeResourceFolder+sep+"indexes"
  val gaeIndexZipFile = gaeResourceFolder+sep+indexZipName
  val gaeAllRhymePartsFile = gaeIndexFolder+sep+allRhymePartsFileName

  def serializedHierarchyResultFile(fileName:String):String={
    rootDataFolder+sep+fileName+".ser"
  }

  def serialisedIndexHierarchyFolder(prefix:String):String={
    serializedIndexesFolder+sep+serialisedIndexHierarchyFileName+prefix+dot+serializedFileExtension
  }

  def serialisedIndexHierarchyPreZipFolder(prefix:String):String={
    new File(serializedIndexesZipFolder).mkdirs
    serializedIndexesZipFolder+sep+serialisedIndexHierarchyFileName+prefix+dot+serializedFileExtension
  }
  
}