package com.rt.rhyme

import com.rt.util.IO
import java.lang.String


class RhymeZoneRhymeMap extends RhymeMap{

  private val rhymeMap:Map[String,List[String]] = makeRhymeMap();

  def makeRhymeMap(): Map[String, List[String]] = {
    IO.fileLines("rhymeZone.txt").foldLeft(Map[String, List[String]]()){
      (map, line) => {
        val key = line.split("-")(0)
        val entries = line.substring(key.length+1, line.length-1).trim
        map(key) = List.fromString(entries, ',').map(_.toUpperCase)
        //map(key) = List.fromString(line.split("-")(1).trim, ',').map(_.toUpperCase)
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

  private def replaceIns(st:String):String={
    if(st.endsWith("IN'"))st.substring(0, st.length-3)+"ING"
    else st
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

  def findReplacement(st:String):Option[String]={
    getFirstWordMutator(st) match{
      case Some(m) => Some(m.modify(st))
      case None => None
    }
  }

  override def doWordsRhyme(oneRaw: String, twoRaw: String): boolean = {
    val one = replaceIns(oneRaw.toUpperCase)
    val two = replaceIns(twoRaw.toUpperCase)

    //println("contains "+one+": "+rhymeMap.contains(one) +", contains "+two+": "+rhymeMap.contains(two)+
    //        ", rhymes: "+(rhymeMap.contains(one) && rhymeMap(one).contains(two)))

    //println("entries for "+one+" are "+rhymeMap(one))

    (rhymeMap.contains(one) && (rhymeMap(one).contains(two))) ||
    (rhymeMap.contains(two) && (rhymeMap(two).contains(one)))
//    if((one == null || two == null) || (one.length == 0 || two.length == 0) || (one == two)){
//      false
//    }
//
//    if(rhymeMap.contains(one) && rhymeMap(one).contains(two)){
//      true
//    }else{
//      val oneReplaced = findReplacement(one).getOrElse(one)
//      val twoReplaced = findReplacement(two).getOrElse(two)
//
//      (rhymeMap.contains(oneReplaced) && rhymeMap(oneReplaced).contains(twoReplaced))
//    }
  }
}