package com.rt.indexing.persistence

import java.io.File


object Constants{
  val artistMetaDataFileName:String = "artist.json"
  val albumMetaDataFileName:String = "album.json"

  val rootDataFolder ="""C:\data\projects\rapAttack\rapAttack"""
  val indexesFolder = rootDataFolder+File.separator+"indexes";

  val dot = "."
  val sep = "/"
  val serializedFileExtension = "ser"
  val serializedIndexesFileName = "index"
  val serialisedIndexHierarchyFileName = "hierarcy-index"
  val serializedIndexesFolder = rootDataFolder+sep+serializedIndexesFileName+dot+serializedFileExtension
  val serialisedIndexHierarchyFolder = indexesFolder+sep+serialisedIndexHierarchyFileName+dot+serializedFileExtension

  def serialisedIndexHierarchyFolder(prefix:String):String={
    indexesFolder+sep+serialisedIndexHierarchyFileName+prefix+dot+serializedFileExtension  
  }
}