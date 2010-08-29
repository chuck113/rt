package com.rt.util

import io.Source
import org.apache.commons.io.IOUtils
import org.slf4j.{Logger, LoggerFactory}
import java.io.{File, FileInputStream, InputStream}
import java.net.{HttpURLConnection, URLConnection, URI}

object IO {
  private val LOG:Logger = LoggerFactory.getLogger("util.IO")   

  def fileLines(file: File): List[String] ={
      readLines(new FileInputStream(file))
  }

  def fileLines(classpathFile: String*): List[String] ={
    classpathFile.toList.foldLeft(List[String]()){(list, f) => {
      list ::: Source.fromInputStream(streamFromClasspath(f)).getLines.toList
    }}
  }

  def readLines(in:InputStream):List[String]={
    val javaList = IOUtils.readLines(in)
    List.fromArray(javaList.toArray).asInstanceOf[List[String]]
  }

  def readLine(in:InputStream):String={
    IOUtils.toString(in)
  }

  def streamFromUrl(url:String):InputStream ={
    val connection:HttpURLConnection = URI.create(url).toURL.openConnection.asInstanceOf[HttpURLConnection]
    connection.getInputStream
  }

  def streamFromClasspath(classpathFile: String): InputStream = {
    val cl: ClassLoader = Thread.currentThread.getContextClassLoader
    val asStream: InputStream = cl.getResourceAsStream(classpathFile)
    if(asStream == null)LOG.warn("Did not get stream for classpath resource "+classpathFile)
    asStream
  }

  def readLinesFromClassPathFile(classpathFile: String): List[String] = {
    readLines(streamFromClasspath(classpathFile))
  }

  def readLineFromClassPathFile(classpathFile: String):String = {
    readLine(streamFromClasspath(classpathFile))
  }

  def fileLines(in:InputStream):List[String]={
    Source.fromInputStream(in).getLines.toList    
  }

  def fileAsString(in:InputStream):String={
    IOUtils.toString(in)
  }

  def fileAsString(file:String):String={
    IOUtils.toString(new FileInputStream(file))
  }


//  // Source.fromFile fails with bufferUnderrun
//  private def htmlAsString(fileName: String): String = {
//    val reader = new BufferedReader(new FileReader(fileName));
//    val b = new StringBuilder();
//    var line: String = "";
//    while (line != null) {
//      line = reader.readLine()
//      if (line != null) {
//        b.append(line + "\n")
//      }
//    }
//    b.toString
//  }
}