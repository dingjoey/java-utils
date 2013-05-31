package com.taobao.joey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-4-26
 * Time: ÏÂÎç3:52
 * To change this template use File | Settings | File Templates.
 */
public class IOUtils {

    private static Logger LOG = LoggerFactory.getLogger(IOUtils.class);

    public static void writeString(OutputStream outputStream, String content, String encoding) throws IOException {
        outputStream.write(content.getBytes(encoding));
    }

    /**
     * read content from reader as whole
     *
     * @param reader
     * @return
     * @throws java.io.IOException
     */
    public static String toString(Reader reader) throws IOException {
        StringWriter sw = new StringWriter();
        copy(reader, sw);
        return sw.toString();
    }

    /**
     * read content from reader by lines
     *
     * @param reader
     * @return
     * @throws java.io.IOException
     */
    public static List<String> readLines(Reader reader) throws IOException {
        BufferedReader bufferedReader = wrapToBufferedReader(reader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private static BufferedReader wrapToBufferedReader(Reader reader) {
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static int copy(Reader reader, Writer writer) throws IOException {
        // allocate 4MB buffer
        char[] buffer = new char[1 << 12];

        int cnt = 0, n = 0;
        while ((n = reader.read(buffer)) >= 0) {
            writer.write(buffer, 0, n);
            cnt += n;
        }

        return cnt;
    }

    public static void main(String[] args) throws IOException {
        File file = new File(".testfiles/testFile");
        OutputStream outputStream = new FileOutputStream(file);
        String content = "haha\nxixi\ngege\n";
        writeString(outputStream, content, "UTF-8");
        outputStream.close();

        Reader reader = new BufferedReader(new FileReader(file));
        reader.mark(1000);
        String readStr = toString(reader);
        LOG.debug(readStr);

        reader.reset();
        List<String> lines = readLines(reader);
        for (String line : lines) {
            LOG.debug("--");
            LOG.debug(line);
        }
        reader.close();
    }
}
