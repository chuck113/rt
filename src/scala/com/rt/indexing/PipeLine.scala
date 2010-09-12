package com.rt.indexing

import collection.immutable.Map
import java.lang.String
import com.rt.hibernate.dto.HibernateMain
import collection.mutable.ListBuffer
import collection.JavaConversions._

object PipeLine{

  val RHYME_SCORE_KEY:String = "RHYME_SCORE_KEY"

  def main(args: Array[String]) {
    //new PipeLine().index("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\methodman-test""")
    //new PipeLine().index("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\atcq-test""")
    new PipeLine().index("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\eminem-test""")
    //new PipeLine().index("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\15Artists""")
    //new PipeLine().index("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\5Artists""")
  }
}

class PipeLine{

  implicit def artistHelper (h:ArtistNode) = new ArtistHelper(h)

  def index(fileRoot:String)={
    val hierarchy: Map[String, ArtistNode] = process(fileRoot)
    println("found "+hierarchy.values.head.children.head.children.head.rhymes.size)

    //if(validate(hierarchy)){
      val persister: HibernateMain = new com.rt.hibernate.dto.HibernateMain()
      persister.perist(new HierarchicalIndexerResult(hierarchy))
    //}
  }

  def process(rootFolder:String):Map[String, ArtistNode]={
    val hierarchy: Map[String, ArtistNode] = new HierarchyBuilder(rootFolder).makeArtistsHierarchy()
    println("found "+hierarchy.values.head.children.head.children.head.rhymes.size)

    processRhymes(hierarchy, new ScoreCalculatorProcessor)     
  }

  private def processRhymes(hierarchy: Map[String, ArtistNode], rhymeProcessor:RhymeProcessor)={
       applyToRhymeParts(hierarchy, rhymeProcessor)
  }

  def applyToRhymeParts(artistFileName: String, song: SongNode, rhymeProcessor:RhymeProcessor): SongNode = {
    new SongNode(song.parent, song.title, song.trackNo, song.rhymes.map(rhyme => rhymeProcessor.transformRhyme(rhyme)))
  }

  def applyToRhymeParts(artistFileName: String, album: AlbumNode, rhymeProcessor:RhymeProcessor): AlbumNode = {
    new AlbumNode(album.parent, album.artist, album.title, album.year, album.children.map(s => applyToRhymeParts(artistFileName, s, rhymeProcessor)))
  }

  def applyToRhymeParts(artistFileName: String, artist: ArtistNode, rhymeProcessor:RhymeProcessor): ArtistNode = {
    new ArtistNode(artist.name, artistFileName, artist.children.map(a => applyToRhymeParts(artistFileName, a, rhymeProcessor)))
  }

  def applyToRhymeParts(hierarchy: Map[String, ArtistNode], rhymeProcessor:RhymeProcessor): Map[String, ArtistNode] = {
    hierarchy.foldLeft(Map[String, ArtistNode]()) {
      (res, entry) => {
        res(entry._1) = applyToRhymeParts(entry._1, entry._2, rhymeProcessor);
      }
    }
  }

    // a bug causes rhymes with one word to be returned.
  def validate(hierarchy:Map[String, ArtistNode]):Boolean={
    println("validating...")
    val valid = hierarchy.values.filter(_.allRhymes.exists(_.parts.length < 2)).size == 0
    if(!valid){
      println("results are INVALID:")

      val rhymeLeaves = hierarchy.values.foldLeft(List[RhymeLeaf]()){(res, artist) => {
        res ::: artist.allRhymes.filter(_.parts.length < 2)
      }}

      rhymeLeaves.foreach(r => println(r))

    }
    else{println("results valid")}

    valid
  }

  class ArtistHelper(artistNode:ArtistNode){
    def allSongs():List[SongNode]= artistNode.children.head.children

    def allRhymes():List[RhymeLeaf]={
      val listBuffer: ListBuffer[RhymeLeaf] = new ListBuffer[RhymeLeaf]()

      artistNode.children.foreach(album => {
        album.children.foreach(song => {
          listBuffer.appendAll(song.rhymes)
        })
      })

      listBuffer.toList
    }
  }
}

trait RhymeProcessor{
  def transformRhyme(rhymeLeaf: RhymeLeaf):RhymeLeaf;
}

class ScoreCalculatorProcessor extends RhymeProcessor{
  def transformRhyme(rhymeLeaf: RhymeLeaf):RhymeLeaf = {    
    RhymeScoreCalculator.calculate(rhymeLeaf.parent.parent.parent.fileName, rhymeLeaf)
  }
}