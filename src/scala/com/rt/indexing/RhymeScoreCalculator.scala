package com.rt.indexing

import com.rt.rhyme.StringRhymeUtils._
import java.lang.String
import collection.mutable.ListBuffer

object RhymeScoreCalculator{


  // rate lowly rhymes with a low part-to-word number ratio -
  //   apply the ratio to the result
  // rate highly rhymes that end with a part
  // rate highly lines that rhyme muliple rhyme sets
  // rate highly rhymes that are followon - more follow-ons the better
  // Dont include the same word rhymed twice
  def calculate(rhymeLeaf:RhymeLeaf):RhymeLeaf={
    val lines: List[String] = rhymeLeaf.lines
    val parts: List[String] = rhymeLeaf.parts;

    var result:Double = 1.0;
    var suitability = 10;
    var lastWordLineAddition = 0;
    val oneMillion = 1000000;
    //result *= parts.length.toDouble
    //result *= averagePartSize(parts)
    result = calculateRhymeSocre(rhymeLeaf)

    if(parts.length > 6){ // reject freak rhymes as they are silly
      //println("parts length was over 4: "+lines +" - "+parts)
      suitability = 0;
    }

    if(isEachLastLineWordAPart(rhymeLeaf) && rhymeLeaf.lines.size > 1){
      //println("last word was rhyme: "+lines +" - "+parts)
      lastWordLineAddition = 10 * oneMillion;
      println("last word rhymes: "+ rhymeLeaf.lines)
    }else if(hasLastWordAsPart(rhymeLeaf)){
      lastWordLineAddition = oneMillion
    }

    if(combinedLengthOfParts(lines) > 170){
      // terrible, one line could have lots of good rhymes in it.
      suitability = 0;
    }

    if(averagePartSize(parts) < 3){ // reject silly small rhymes
      //println("ave parts length was less than 3: "+lines +" - "+parts)
      suitability = 0;
    }

    if(partToWordsRatio(parts, asLine(lines)) < 0.1){// reject sparse rhymes
      //println("rhyme ratio was less than 0.1: "+parts +" - "+lines)
      suitability = 0;
    }

    val finalResult = (result * suitability).toInt + lastWordLineAddition;
    //println("ratio is "+partToWordsRatio(parts, asLine(lines))+", parts length is "+parts.length+" ave parts: "+averagePartSize(parts) )
    return new RhymeLeaf(rhymeLeaf.word, rhymeLeaf.lines, rhymeLeaf.parts, finalResult, suitability, 0);
  }

  def calculateRhymeSocre(rhymeLeaf:RhymeLeaf):Int={
    val found:ListBuffer[String] = new ListBuffer[String];
    var res = 0;
    rhymeLeaf.parts.removeDuplicates.foreach(part =>{
    //rhymeLeaf.parts.foldLeft(0){(res, part) => {
      if(!found.contains(part)){
        res += (part.length*part.length)
        found += part
      }else{
        println("already found "+part)
      }
      //res
    })
    return res
  }

  // nees to include follow-on rhymes
  def hasLastWordAsPart(rhymeLeaf:RhymeLeaf):Boolean={
    rhymeLeaf.parts.contains(individualWords(rhymeLeaf.lines).last)
  }

  def numberOfLinesWithAPartAsLastWord(rhymeLeaf:RhymeLeaf):Int ={
    rhymeLeaf.lines.filter(line => rhymeLeaf.parts.contains(individualWords(line).last.toUpperCase)).size
  }

  def isEachLastLineWordAPart(rhymeLeaf:RhymeLeaf):Boolean={
    rhymeLeaf.lines.forall(line => rhymeLeaf.parts.contains(individualWords(line).last.toUpperCase));     
  }

  def isFollowOn(rhymeLeaf:RhymeLeaf):Boolean={
    FollowOnProcessor.isFollowOn(rhymeLeaf)
  }

  def averagePartSize(parts:List[String]):Double={
    combinedLengthOfParts(parts).toDouble / parts.length.toDouble
  }

  def combinedLengthOfParts(parts:List[String]):Int={
     parts.foldLeft(0)((res, st) => res + st.length)
  }

  def partToWordsRatio(parts: List[String], line:String):Double={
    parts.size.toDouble / line.split(" ").size.toDouble
  }

  def asLine(list: List[String]):String={
    list.foldLeft(""){(st, listEntry) =>{
      st.concat(listEntry)
    }}
  }
}