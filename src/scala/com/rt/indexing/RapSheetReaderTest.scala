package indexing

import collection.mutable.ListBuffer
import org.junit.Test
import org.scalatest.junit.{JUnitSuite, AssertionsForJUnit}

class RapSheetReaderTest extends JUnitSuite  {

  val reader = new RapSheetReader()

  @Test def twoLineRhyme(){
    val lines = List("Foot on the pedal - never ever false metal","Engine running hotter than a boiling kettle")
    val res = reader.findMultiLineRhymes(lines)
    println("res is "+res)
    assert(res.size > 0)
  }

  @Test def oneLineRhyme(){
    val line = "My mic check is life or death, breathin a sniper's breath"
    val res = reader.findSingleLineRhymes(List(line))
    println("res is "+res)
    assert(res.size > 0)
  }

  @Test def oneLineRhyme2(){
    val line = "in I keep all my rhymes in my Le Sportsac"
    val res = reader.findSingleLineRhymes(List(line))
    println("res is "+res)
    assert(res.size > 0)
  }

}