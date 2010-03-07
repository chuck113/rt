package util

import io.Source

object IO {
  def fileLines(classpathFile: String): List[String] ={
      Source.fromInputStream(getClass().getClassLoader().getResourceAsStream(classpathFile)).getLines.toList
  }
}