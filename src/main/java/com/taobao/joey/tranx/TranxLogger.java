package com.taobao.joey.tranx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: ����3:53
 * <p/>
 * <p/>
 * ÿ����Ҫ���в����־û���ҵ�񳡾�����һ��TranxLoggerʵ��
 * tranxNameΪ��Ӧҵ�񳡾�������,��Ҳ���������ڱ�֤�־��Ե���־�ļ���Ŀ¼����
 */
public class TranxLogger implements ITranxLogger {

    private static final String BASE_DIR = "transaction-log";
    private static final String FILE_NAME_SEPERATOR = ".";
    /**
     * ����id����Ч��Χ
     * 1-9223372036854775807
     */
    private AtomicLong tranxId = new AtomicLong(0);
    /**
     *
     */
    private volatile long syncTranxId = Long.MIN_VALUE;

    private String root;
    private String tranxName;
    private TranxLogFileOutputStream outputStream;
    private TranxLogFileInputStream inputStream;


    public void setRoot(String root) {
        this.root = root;
    }

    public void setTranxName(String tranxName) {
        this.tranxName = tranxName;
    }

    private String genLogName(long index) {
        StringBuilder sb = new StringBuilder();
        sb.append("log");
        sb.append(FILE_NAME_SEPERATOR);
        sb.append(index);
        sb.append(FILE_NAME_SEPERATOR);
        sb.append(System.currentTimeMillis());
        return sb.toString();
    }

    private void findActive() {

    }

    private void create() throws IOException {
        File parent = new File(root, BASE_DIR);
        File tranxDir = new File(parent, tranxName);

        if (!tranxDir.exists()) {
            tranxDir.mkdirs();
        }

        File logFile = new File(tranxDir, genLogName(tranxId.getAndIncrement()));
        logFile.createNewFile();
    }

    private void delete(File file) {
        file.delete();
    }

    /**
     * ɨ����־Ŀ¼�µ�����Ŀ¼�ļ�����д����
     *
     * @throws FileNotFoundException
     */
    public void openLog() throws FileNotFoundException {
        File parent = new File(root, BASE_DIR);
        File tranxDir = new File(parent, tranxName);

        if (tranxDir.listFiles() == null) {
            return;
        }


    }

    /**
     * �ر�д���������ұ�֤buffer sync to disk
     *
     * @throws IOException
     */
    public void closeLog() throws IOException {
        outputStream.close();
    }

    final public void sync() {

    }

    final public void checkpoint() {

    }

    @Override
    final public void log(Loggable loggable) {
        List<Loggable> loggables = new ArrayList<Loggable>(1);
        innerLog(loggables);
    }

    @Override
    final public void logTranx(List<Loggable> tranx) {
        innerLog(tranx);
    }

    private void innerLog(List<Loggable> loggables) {

    }

    private static class TranxLogFileOutputStream {
        private File file;
        private FileOutputStream fos;
        private DataOutputBuffer buffer;

        private TranxLogFileOutputStream(File file) throws FileNotFoundException {
            this.file = file;
            fos = new FileOutputStream(file);
            buffer = new DataOutputBuffer();
        }

        public synchronized void write(Loggable loggable) {
            buffer.writeLoggable(loggable);
        }

        public synchronized void flushAndSync() throws IOException {
            // write and flush
            fos.write(buffer.toByteArray());
            fos.flush();
            // sync to disk
            FileDescriptor fd = fos.getFD();
            fd.sync();
            // reset the buffer for reuse
            buffer.reset();
        }

        public synchronized void close() throws IOException {
            flushAndSync();
            fos.close();
        }
    }

    private static class TranxLogFileInputStream {
        private File file;
        private FileInputStream fis;

        private TranxLogFileInputStream(File file) throws FileNotFoundException {
            this.file = file;
            fis = new FileInputStream(file);
        }

        public void read(Loggable loggable) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            DataInputBuffer buffer = new DataInputBuffer(line.getBytes());
            buffer.readLoggable(loggable);
        }
    }

}
