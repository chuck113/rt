package util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public void zip(String folderToZip, String outFile) {
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
