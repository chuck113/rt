package com.rt.dto;

import com.rt.indexing.RhymeLines;

import java.io.*;
import java.util.List;
import java.util.Map;

public class DataMapper {

    public void write(Object obj, OutputStream out){
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public <T> T read(InputStream in){
        try {
            ObjectInputStream inputStream = new ObjectInputStream(in);
            return (T)inputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
