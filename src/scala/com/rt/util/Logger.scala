package com.rt.util



object Logger{

  def debug(st:String){
    write(st)
  }

  def progress(st:String){
    write(st)
  }

  private def write(st:String){
    println(st)
  }
}