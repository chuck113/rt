package com.rt.tools

import com.rt.indexing.persistence.Constants
import com.rt.ohhla.OhhlaFiles
import java.lang.String
import java.io.{FileFilter, File}
import com.rt.util.{ScalaConversions, NameMapper, IO}
import java.util.{Set => jSet}
import com.rt.rhyme.{SongFileParser, CmuDictRhymeMap, Song, RapSheetReader}

class PrintEveryOhhlaWord{

  private val toRemove:List[String] = List("\\", "/", "(", ")", ".", ":", ";", "*", "?", ",", "\"", "-", "+", "!", "$", "%", "[", "]", "{", "}", "_", "=", "~");

  def cleanWord(st:String):String={
    var result:String = st
    toRemove.foreach(r => {result = st.replace(r, "")})
    result
  }
  
  def printEveryWord()={
    val cmuDict: scala.collection.Set[String] = allCmuDictWords()
    val javaSet:jSet[String] = new java.util.HashSet[String]()
    val targetLocation: String = OhhlaFiles.root
    val reader:SongFileParser = new SongFileParser();
    val folders: List[File] = foldersInDir(new File(targetLocation))
    folders.foreach(artistFolder =>{
      val albumFolders: List[File] = foldersInDir(artistFolder)      
      albumFolders.foreach(artistFolder =>{
        val list: List[File] = textFilesInFolder(artistFolder)
        list.foreach(file => {
          val lines: List[String] = IO.fileLines(file)
          val song: Song = reader.buildSong(lines)
          song.verses.foreach(v =>{
            val verseLines:List[String] = v.lines
            verseLines.foreach(vLine => {
              vLine.split("\\s").toList.foreach(w =>{
                val word:String = cleanWord(w)
                //println(word+" contained: "+cmuDict.contains(word))
                if(cmuDict.contains(word)) {
                  javaSet.add(word)
                }
              })
            })
          })
        })
      })
    })

    List(javaSet.toArray: _*).foreach(println)
  }

  def allCmuDictWords():scala.collection.Set[String]={
    CmuDictRhymeMap.getRhymeMap.keySet
  }

  def dirFilter = new FileFilter {
    def accept(f: File) = f.isDirectory
  }

  def textFileFilter = new FileFilter {
    def accept(f: File) = f.getName.endsWith(".txt")
  }

  def textFilesInFolder(dir: File): List[File] = {
    dir.listFiles(textFileFilter).toList
  }

  def foldersInDir(dir: File): List[File] = {
    dir.listFiles(dirFilter).toList
  }

  def filesInDir(dir: File): List[File] = {
    dir.listFiles().toList
  }
}