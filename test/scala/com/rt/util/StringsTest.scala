package com.rt.util

import org.junit.Test
import junit.framework.Assert._

class StringsTest {
  @Test def ShouldTrimPunctuationFromStart() {
    assertEquals("word", Strings.trimPunctuation(".,word"))
  }

  @Test def ShouldTrimPunctuation() {
    assertEquals("word", Strings.trimPunctuation(".,word.,"))
  }

  @Test def ShouldWord() {
    assertEquals("", Strings.trimPunctuation(""))
    assertEquals("", Strings.trimPunctuation("."))
    assertEquals("", Strings.trimPunctuation(null))
  }
}