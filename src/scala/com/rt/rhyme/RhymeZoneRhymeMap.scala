package com.rt.rhyme

import com.rt.util.IO
import java.lang.String
import collection.mutable.HashMap
import collection.immutable.HashSet


class RhymeZoneRhymeMap extends RhymeMap{
  
  private val rhymeMap:Map[String,List[String]] = makeRhymeMap();
  private val aliasMap:Map[String, String] = makeAliasMap();
  //private val aliasMap:Map[String, String] = Map[String, String]()//makeAliasMap();

  def makeRhymeMap(): Map[String, List[String]] = {
    println("setting up map...")
    IO.fileLines("rhymeZone.txt").foldLeft(Map[String, List[String]]()){
      (map, line) => {
        val key = line.split("-")(0)
        val entries = line.substring(key.length+1, line.length).trim
        map(key) = List.fromString(entries, ',').map(_.toUpperCase)
        //map(key) = List.fromString(line.split("-")(1).trim, ',').map(_.toUpperCase)
      }
    }
  }

  def makeAliasMap():Map[String,String]={
    val filesToRead:List[String] = List[String]("rhymeAliases-generated.txt", "rhymeAliases-manual.txt");
    val lines:List[String] = filesToRead.foldLeft(List[String]()){(res, file) => {
      res ++ IO.fileLines(file)
    }}

    lines.filter(_.length > 0).filter(_.split(",").length > 1).foldLeft(Map[String, String]()){
      (map, line) => {
        val key = line.split(",")(0).trim
        val value = line.split(",")(1).trim
        map(key) = value
      }
    }
  }

  abstract class WordMutator{
    def matches(st:String):Boolean
    def modify(st:String):String
  }

  private case class IngMutator extends WordMutator{
    def matches(st: String) = st.endsWith("IN'")
    def modify(st: String) = st.substring(0, st.length-3)+"ING"
  }


  private case class AddPluralMutator extends WordMutator{
    def matches(st: String) = !st.endsWith("S'")
    def modify(st: String) = st+"S"
  }

  private case class AddPluralMutator2 extends WordMutator{
    def matches(st: String) = !st.endsWith("ES'")
    def modify(st: String) = st+"ES"
  }

  private case class RemovePluralMutator extends WordMutator{
    def matches(st: String) = st.endsWith("S")
    def modify(st: String) = if(st.length < 1)st else st.substring(0, st.length-1)
  }

  private case class RemovePluralMutator2 extends WordMutator{
    def matches(st: String) = st.endsWith("ES")
    def modify(st: String) = if(st.length < 2)st else st.substring(0, st.length-2)
  }

  private def mutators:List[WordMutator] = List(IngMutator(), AddPluralMutator(), AddPluralMutator2(), RemovePluralMutator(), RemovePluralMutator2())

  private def getFirstWordMutator(st:String):Option[WordMutator]={
    //println("getFirstWordMutator: "+st+": "+mutators.find(_.matches(st))+", found: "+(rhymeMap.contains(_.modify(st))))
    mutators.find(m => {
      m.matches(st) && rhymeMap.contains(m.modify(st))
    })
  }

  def knownWords():Set[String]={
    return new HashSet() ++ rhymeMap.keySet ++ aliasMap.keySet
  }

  def findReplacement(st:String):Option[String]={
    getFirstWordMutator(st) match{
      case Some(m) => Some(m.modify(st))
      case None => None
    }
  }

  /** deal with words like 'singin' */
//  private def replaceIns(word:String):String ={
//    if(!rhymeMap.contains(word) && rhymeMap.contains(word+"G")){
//      word+"G"
//    }else{
//      StringRhymeUtils.replaceIns(word)
//    }
//  }

  override def doWordsRhyme(oneRawUncassed: String, twoRawUncassed: String): Boolean = {
    val oneRaw = oneRawUncassed.toUpperCase
    val twoRaw = twoRawUncassed.toUpperCase

    val one:String = if(rhymeMap.contains(oneRaw)) oneRaw else aliasMap.getOrElse(oneRaw, oneRaw)
    val two:String = if(rhymeMap.contains(twoRaw)) twoRaw else aliasMap.getOrElse(twoRaw, twoRaw)

//    if(aliasMap.contains(one)){
//      println("found in alias");
//    }

//    println(one+" one: "+rhymeMap(one))
//
//    println("doWordsRhyme: "+ one+", "+two+" rhymes: "+((rhymeMap.contains(one) && (rhymeMap(one).contains(two))) ||
//      (rhymeMap.contains(two) && (rhymeMap(two).contains(one)))))

    (rhymeMap.contains(one) && (rhymeMap(one).contains(two))) ||
    (rhymeMap.contains(two) && (rhymeMap(two).contains(one)))
  }
}