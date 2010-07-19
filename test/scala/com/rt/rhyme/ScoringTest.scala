package com.rt.rhyme

import org.junit.Test
import junit.framework.Assert._
import com.rt.indexing.{RhymeLeaf, RhymeScoreCalculator}

class ScoringTest{

  //private val calculator:RhymeScoreCalculator = new RhymeScoreCalculator();

  @Test def testScoreIsCorrect={
    val leaf = new RhymeLeaf("CRACK",
      List[String]("Niggaz wanna know, how I live the mack life", "Making money smoking mics like crack pipes"),
      List[String]("CRACK", "MACK"))

    val result = RhymeScoreCalculator.calculate(leaf)
    println("result is "+result.rating);
  }

}