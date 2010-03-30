package com.rt.indexing

import collection.mutable.ListBuffer
import org.junit.Test
import junit.framework.Assert
import com.rt.rhyme.RhymeFinder

class RhymeFinderTest  {

  val reader = new RhymeFinder()

  @Test def twoLineRhyme(){
    val lines = List("Foot on the pedal - never ever false metal","Engine running hotter than a boiling kettle")
    val res = reader.findMultiLineRhymes(lines)
    println("res is "+res)
    Assert.assertTrue(res.size > 0)
  }

  @Test def oneLineRhyme(){
    val line = "My mic check is life or death, breathin a sniper's breath"
    val res = reader.findSingleLineRhymes(List(line))
    println("res is "+res)
    Assert.assertTrue(res.size > 0)
  }

  @Test def oneLineRhyme2(){
    val line = "in I keep all my rhymes in my Le Sportsac"
    val res = reader.findSingleLineRhymes(List(line))
    println("res is "+res)
    Assert.assertTrue(res.size > 0)
  }


}