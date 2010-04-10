package com.rt.dto;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DataMapper {

    public void write(Object obj, OutputStream out) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public <T> T read(InputStream in) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(in);
            return (T) inputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static void unzip(String zipFile, String targetFolder) {
        try {
            String destinationname = targetFolder;
            byte[] buf = new byte[1024];
            ZipInputStream zipinputstream = null;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(new FileInputStream(zipFile));

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = zipentry.getName();
                System.out.println("entryname " + entryName);
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                String directory = newFile.getParent();

                if (directory == null) {
                    if (newFile.isDirectory())
                        break;
                }

                fileoutputstream = new FileOutputStream(destinationname+"/" + entryName);

                fileoutputstream.getChannel();

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
                    fileoutputstream.write(buf, 0, n);

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }//while

            zipinputstream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO move this to ZipUtil - scala doesn't seem to pick it up. 
    public static void zip(String folderToZip, String outFile) {
        try {
            File inFolder = new File(folderToZip);
            File outFolder = new File(outFile);
            ZipOutputStream out = new ZipOutputStream(new
                    BufferedOutputStream(new FileOutputStream(outFolder)));
            BufferedInputStream in = null;
            byte[] data = new byte[1000];
            String files[] = inFolder.list();
            for (int i = 0; i < files.length; i++) {
                in = new BufferedInputStream(new FileInputStream
                        (inFolder.getPath() + "/" + files[i]), 1000);
                out.putNextEntry(new ZipEntry(files[i]));
                int count;
                while ((count = in.read(data, 0, 1000)) != -1) {
                    out.write(data, 0, count);
                }
                out.closeEntry();
            }
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
