package com.rt.indexing

import util.IO

object CMUDict{

  private val rhymeMap = new CMUDict().makeRhymeMap();

  //TODO make this do more work, ie actually calculate rhymes
  def getRhymeMap():Map[String, String] ={
    rhymeMap
  }
}

class CMUDict {

  def validEntry(entry: String):boolean ={
    !(entry.startsWith("#") ||entry.length==0 || entry == ("\n") )
  }

  def makeRhymeMap(): Map[String, String] = {
    IO.fileLines("cmudict.0.6-2.txt").filter(validEntry).foldLeft(Map[String, String]()){
      (map, line) => {
        val key = line.split(" ")(0)
        //println("line is '"+line+"'")
        map(key) = line.substring(key.length+2, line.length-1)
      }
    }
  }
}