package com.rt.util

import java.io.{FileFilter, File}

object Folders {
  def folderNamesInFolder(rootFolder:String):List[String] ={
    folderNamesInFolderToFiles(rootFolder).keys.toList
  }

  def folderNamesInFolderToFiles(rootFolder:String):Map[String,File] ={
     foldersInDir(new File(rootFolder)).foldLeft(Map[String,File]()){(map, folder)=>{
      map(folder.getName) = folder
    }}
  }

  //f: (double) => double
  def folderNamesInFolderToFiles(rootFolder:String, fileNameTransformer:(String) => String):Map[String,File] ={
     foldersInDir(new File(rootFolder)).foldLeft(Map[String,File]()){(map, folder)=>{
      map(fileNameTransformer(folder.getName)) = folder
    }}
  }

  def foldersInDir(dir: File): List[File] = {
    dir.listFiles(dirFilter).toList
  }

  def foldersInDir(dir: String): List[File] = {
    new File(dir).listFiles(dirFilter).toList
  }

  def dirFilter = new FileFilter {
    def accept(f: File) = f.isDirectory
  }
}