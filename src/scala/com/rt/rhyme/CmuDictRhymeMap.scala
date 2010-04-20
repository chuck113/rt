package com.rt.rhyme

import util.IO
import com.rt.util.IO


object CmuDictRhymeMap{

  private val rhymeMap = new CmuDictRhymeMap().makeRhymeMap();

  def getRhymeMap():Map[String, String] ={
    rhymeMap
  }
}

class CmuDictRhymeMap  extends RhymeMap{

  private val rhymeMap:Map[String,String] = makeRhymeMap();

  private def validEntry(entry: String):Boolean ={
    !(entry.startsWith("#") ||entry.length==0 || entry == ("\n") )
  }

  def makeRhymeMap(): Map[String, String] = {
    IO.fileLines("cmudict.0.6-2.txt", "cmudict-extensions.txt").filter(validEntry).foldLeft(Map[String, String]()){
      (map, line) => {
        val key = line.split(" ")(0)
        //println("line is '"+line+"'")
        map(key) = line.substring(key.length+2, line.length-1)
      }
    }
  }

  def containsParts(part1:String, part2:String):Boolean={
    rhymeMap.contains(part1) && rhymeMap.contains(part2)
  }

  def apply(part:String):String={
    rhymeMap(part)
  }


  /**
   * Given 2 lines find out if they rhyme by looking at
   * the last word in each line, Words must be in uppercase
   */
  override def doWordsRhyme(one: String, two: String): Boolean = {
    val oneUpper = one.toUpperCase()
    val twoUpper = two.toUpperCase()
    //if ((oneUpper != twoUpper) && (rhymeMap.contains(oneUpper) && rhymeMap.contains(twoUpper))) {
    if ((oneUpper != twoUpper) && containsParts(oneUpper, twoUpper)){
      //println("oneUpper= "+oneUpper+", map value: "+ rhymeMap(oneUpper)+", reversed: "+ rhymeMap(oneUpper).split(" ").reverse.elements)
      val it1 = rhymeMap(oneUpper).split(" ").reverse.elements
      val it2 = rhymeMap(twoUpper).split(" ").reverse.elements

      doLastRhymeAndPrecedingLettersEqual(it1.zip(it2).toList)
    } else {
      false
    }
  }

  private def doLastRhymeAndPrecedingLettersEqual(pairs: List[(String, String)]): Boolean = {
    val equals = pairs.takeWhile(pair => (pair._1 == pair._2)).map(_._1)
    equals.size match {
      case 0 => false
      case _ => equals.findIndexOf(isRhymePart _) != -1
    }
  }

  private def isRhymePart(part: String): Boolean = {
    part.charAt(part.length - 1) match {
      case '0' => true
      case '1' => true;
      case '2' => true;
      case _ => false
    }
  }
}