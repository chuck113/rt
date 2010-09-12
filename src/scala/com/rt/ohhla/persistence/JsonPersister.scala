package com.rt.ohhla.persistence

import java.io.{FileWriter, Writer, OutputStream}

class JsonPersister{

  def writeToFile(file:String, json:String):Unit ={
    write(new FileWriter(file), json)  
  }

  def write(out:Writer, json:String):Unit ={
    out.write(json)
    out.flush()
    out.close()
  }
}