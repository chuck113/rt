package indexing

import util.IO

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 10-Feb-2010
 * Time: 12:32:18
 * To change this template use File | Settings | File Templates.
 */

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