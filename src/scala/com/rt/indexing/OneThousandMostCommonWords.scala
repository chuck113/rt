package com.rt.indexing

import io.Source
import java.io.FileInputStream
import util.IO

class OneThousandMostCommonWords{
  private def getMostCommonWords(): Set[String] = {
    IO.fileLines("1000-most-used-English-words.txt").foldLeft(Set[String]()){
      (set, line) =>{
        set ++ line.split(" ").foldLeft(Set[String]()){
          (innerSet, word) =>{
             innerSet + word.trim.toUpperCase
          }
        }
      }
    }
  }

  def makeRhymeMap(): Map[String, String] = {
    val wordsToFind = getMostCommonWords()
    val contained = IO.fileLines("cmudict.0.6-2.txt").filter(line => {
      wordsToFind.contains(line.split(" ")(0))
    })

    contained.foldLeft(Map[String, String]()){
      (map, line) => {
        val key = line.split(" ")(0)
        map(key) = line.substring(key.length+2, line.length-1)
      }
    }
  }

  
}
object OneThousandMostCommonWords{
  def main(args: Array[String]): Unit = {
      //val set  = get()
      //val list = set.toList.sort((e1, e2) => (e1 compareTo e2) < 0).filter("[\\W]+".r.findFirstIn(_).isEmpty)

      //list.foreach(l => println(l))
      println(new OneThousandMostCommonWords().makeRhymeMap())
    }
}