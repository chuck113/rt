package com.rt.indexing

import com.rt.rhyme.StringRhymeUtils._
import java.lang.String
import collection.mutable.ListBuffer

object FollowOnProcessor {
  def main(args: Array[String]): Unit = {
//    val leaf = RhymeLeaf("lie",
//          List[String]("She'll invite me, politely, to fight G, And then we lie together, cry together, I swear to God I hope we fuckin die together"),
//          List[String]("lie", "cry", "die"), 0, 0)
//
    val leaf = RhymeLeaf("shoot",
          List[String]("Not stories by Aesop, place your loot up, parties I shoot up"),
          List[String]("loot", "shoot"), 0, 0, 0)

    println(isFollowOn(leaf))
  }

  //TODO does not support a rhymeLeaf containing muliple rhyme sets
  def isFollowOn(rhyme: RhymeLeaf): Boolean = {
    val words: List[String] = individualWords(rhyme.lines)
    //val line:String = rhyme.lines.foldLeft(""){_+_}
    val nextWords:ListBuffer[String] = new ListBuffer[String]

    rhyme.parts.foreach(p =>{
      val index:Int = words.indexOf(p.toUpperCase)
      if(index < words.size){
        nextWords.append(words(index+1).toUpperCase)
      }
    })

    // simple follow on - all parts are equal. Others may test for:
    // - two of three parts having follow-ons
    // follow ns that rhyme
    areAllWordsEqual(nextWords.toList)
  }

  def areAllWordsEqual(words:List[String]):Boolean={
    words.size > 1 && words.removeDuplicates.size == 1
  }


}