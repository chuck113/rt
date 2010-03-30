package util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JavaIo {
    public List<String> readLines(InputStream in) throws IOException {
        return (List<String>)IOUtils.readLines(in);
    }
}
