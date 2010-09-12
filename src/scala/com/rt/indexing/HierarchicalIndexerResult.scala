package com.rt.indexing

import java.lang.String
import collection.immutable.Map
import collection.mutable.ListBuffer
import java.io._
import collection.JavaConversions._

case class HierarchicalIndexerResult(index: Map[String, ArtistNode]) extends Serializable {

  def rhymeListJ(): java.util.List[RhymeLeaf] = rhymeList
  def getAllRhymePartsJ(): java.util.Set[String] = getAllRhymeParts
  def indexJ(): java.util.Map[String, ArtistNode] = index

  def rhymeList(): List[RhymeLeaf] = {
    val rhymeBuffer: ListBuffer[RhymeLeaf] = new ListBuffer[RhymeLeaf]();
    index.values.foreach(artist => {
      artist.children.foreach(album => {
        album.children.foreach(song => {
          song.rhymes.foreach(rhyme => {
            rhymeBuffer.append(rhyme)
          })
        })
      })
    })
    rhymeBuffer.toList
  }

  def getAllRhymeParts(): Set[String] = {
    val listBuffer: ListBuffer[String] = new ListBuffer[String]()
    index.values.foreach(artist => {
      artist.children.foreach(album => {
        album.children.foreach(song => {
          song.rhymes.foreach(rhyme => {
            listBuffer.appendAll(rhyme.parts)
          })
        })
      })
    })

    listBuffer.toList.removeDuplicates.toSet[String]
  }
}




















