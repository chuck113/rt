package com.rt.indexing

class PipeLine{
  def index(fileRoot:String)={
    val indexer: HierarchicalIndexer = new HierarchicalIndexer()
    indexer.makeArtistHierarchyWithAllWords()
  }
}