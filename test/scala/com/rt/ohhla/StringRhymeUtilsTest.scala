package com.rt.ohhla

import org.junit.Assert._
import org.junit.{Assert, Before, Test}
import com.rt.rhyme.StringRhymeUtils

class StringRhymeUtilsTest{
  @Test def shouldReturnAllTextOutsideOfBrackets(){
    val line = "before (in brackets) after"
    assertEquals("before after", StringRhymeUtils.removeBrackets(line));  
  }

  @Test def shouldReturnAllTextBeforeBrackets(){
    val line = "before (in brackets)"
    assertEquals("before", StringRhymeUtils.removeBrackets(line));
  }

  @Test def shouldReturnAllTextAfterBrackets(){
    val line = "(in brackets) after"
    assertEquals("after", StringRhymeUtils.removeBrackets(line));
  }

  @Test def shouldReturnTextIfContainsOneBracket(){
    val line = "some ( text"
    assertEquals(line, StringRhymeUtils.removeBrackets(line));
  }

  @Test def shouldReturnTextIfContainsMisplacedBrackets(){
    val line = ")some ( text"
    assertEquals(line, StringRhymeUtils.removeBrackets(line));
  }

  @Test def shouldReturnTextIfDoesNotContainBrackets(){
    val line = "some text"
    assertEquals(line, StringRhymeUtils.removeBrackets(line));  
  }
}