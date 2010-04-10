package com.rt.indexing.persistence

import java.io.File


object Constants{
  val artistMetaDataFileName:String = "artist.json"
  val albumMetaDataFileName:String = "album.json"

  val rootDataFolder ="""C:\data\projects\rapAttack\rapAttack"""
  val gaeProjectFolder ="""C:\data\projects\rapAttack\rapAttackGAE"""
  val indexesFolder = rootDataFolder+File.separator+"indexes";

  val dot = "."
  val sep = "/"
  val gaeResourceFolderName = "resources"
  val serializedFileExtension = "ser"
  val serializedIndexesFileName = "index"
  val serializedIndexesZipFolderName = "index"
  val serialisedIndexHierarchyFileName = "hierarcy-index"
  //val serializedIndexesFolder = rootDataFolder+sep+serializedIndexesFileName+dot+serializedFileExtension
  //val serialisedIndexHierarchyFolder = indexesFolder+sep+serialisedIndexHierarchyFileName+dot+serializedFileExtension
  val indexZipName = "index.zip"
  val serialisedIndexHierarchyZipFile = indexesFolder+sep+indexZipName
  val serializedIndexesZipFolder = indexesFolder+sep+serializedIndexesZipFolderName
  val gaeResourceFolder = gaeProjectFolder+sep+gaeResourceFolderName
  val gaeIndexFolder = gaeResourceFolder+sep+"indexes"
  val gaeIndexZipFile = gaeResourceFolder+sep+indexZipName


  def serialisedIndexHierarchyFolder(prefix:String):String={
    indexesFolder+sep+serialisedIndexHierarchyFileName+prefix+dot+serializedFileExtension  
  }

  def serialisedIndexHierarchyPreZipFolder(prefix:String):String={
    new File(indexesFolder+sep+serializedIndexesZipFolderName).mkdirs
    indexesFolder+sep+serializedIndexesZipFolderName+sep+serialisedIndexHierarchyFileName+prefix+dot+serializedFileExtension
  }
  
}