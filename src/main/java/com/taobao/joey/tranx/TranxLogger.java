package com.taobao.joey.tranx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
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
    /**
     * ����id����Ч��Χ
     * 1-9223372036854775807
     */
    public static final AtomicLong tranxId = new AtomicLong(0l);
    private static final Logger LOG = LoggerFactory.getLogger(TranxLogger.class);
    /**
     * ��־�ļ�·��
     * $root/transaction-log/$tranxName/log.$tranxId.$timestamp
     */
    private static final String BASE_DIR = "transaction-log";
    private static final String FILE_NAME_SEPERATOR = ".";
    private static final String FILE_NAME_SEPERATOR_REX = "\\.";
    private static final String DEFAULT_TRANX_NAME = "default-transaction";
    /**
     * ����һ����־�ļ���С������Ϊ��¼LOG_MAX_TRANX_NUM������
     */
    private static final int LOG_MAX_TRANX_NUM = 1000;
    /**
     * ����TranxLoggerʵ���Ļ��� key == root + FILE_NAME_SEPERATOR +  tranxName
     */
    private static final Map<String, TranxLogger> tranxLoggers = new HashMap<String, TranxLogger>();
    private String root;
    private String tranxName;
    /**
     * ���µ�һ��������־��¼sync�����̵ĵ�transaction id
     */
    private volatile long syncTranxId = Long.MIN_VALUE;
    /**
     * ��־�ļ������
     */
    private volatile TranxLogFileOutputStream outputStream;  //AtomicReference����һ�������Ǹ��׶�
    /**
     * ��־�ļ�������
     */
    private TranxLogFileInputStream inputStream;

    private TranxLogger(String root, String tranxName) {
        this.root = root;
        this.tranxName = tranxName;

        if (root == null || root.isEmpty()) {
            root = ".";
        }
        if (tranxName == null || tranxName.isEmpty()) {
            tranxName = DEFAULT_TRANX_NAME;
        }

        //TODO to verfiy the tranxName must be unique
    }

    /**
     * @return
     */
    public static long nextTranxId() {
        return tranxId.getAndIncrement();
    }

    final public static synchronized TranxLogger getTranxLogger(String root, String tranxName) {
        String key = new StringBuilder().append(root).append(FILE_NAME_SEPERATOR).append(tranxName).toString();
        TranxLogger logger = tranxLoggers.get(key);

        if (logger == null) {
            tranxLoggers.put(key, logger = new TranxLogger(root, tranxName));
        }

        return logger;
    }

    /**
     * ɨ����־Ŀ¼�µ�����Ŀ¼�ļ�����λ��־β����д����
     *
     * @return
     */
    final public synchronized boolean openLogging() {
        // �ҵ���־�ļ�β;�������
        File activeLog = findActiveLog();
        if (activeLog == null) {  // û����־�ļ��򴴽�һ���µ�
            try {
                activeLog = create();
            } catch (IOException e) {
                LOG.error("create log file error", e);
                return false;
            } catch (IllegalStateException e) {
                LOG.error("create log file error", e);
                return false;
            }
        }

        try {
            outputStream = new TranxLogFileOutputStream(activeLog);
        } catch (FileNotFoundException e) {
            LOG.error("active log file not found", e);
            return false;
        }

        return true;
    }

    /**
     * �ر�д���������ұ�֤buffer sync to disk
     */
    final public synchronized boolean closeLogging() {
        if (outputStream == null) {
            return false;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            LOG.warn("close tranx logger error, log records maybe lost", e);
            return false;
        }
        return true;
    }

    /**
     * @param steps
     * @return
     */
    final public boolean openRecovering(int steps) {
        // ȷ����־Ŀ¼����
        File parent = new File(root, BASE_DIR);
        File tranxDir = new File(parent, tranxName);
        if (!tranxDir.exists()) {
            return false;
        }

        // �ҵ���־�ļ�β;��������
        File activeLog = findActiveLog();
        if (activeLog == null) {
            return false;
        }

        try {
            inputStream = new TranxLogFileInputStream(activeLog, steps);
        } catch (IOException e) {
            LOG.warn("", e);
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    final public boolean closeRecovering() {
        if (inputStream == null) {
            return false;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    final public boolean sync() {
        if (outputStream == null) {
            return false;
        }
        try {
            outputStream.flushAndSync();
        } catch (IOException e) {
            try {
                LOG.warn("sync falied, data maybe lost :  {} ", outputStream.buffer.dump("UTF-8"), e);
            } catch (UnsupportedEncodingException e1) {
            }
            return false;
        }
        return true;
    }

    final boolean checkpoint() {
        //TODO
        return true;
    }

    final public void log(Loggable loggable) {
        if (loggable == null) return;
        List<Loggable> loggables = new ArrayList<Loggable>(1);
        checkLogFull();
        innerLog(loggables);
    }

    final public void logTranx(List<Loggable> tranx) {
        if (tranx == null || tranx.size() == 0) return;
        checkLogFull();
        innerLog(tranx);
    }

    final public boolean recover() throws IOException {
        List<Loggable> tranx = new ArrayList<Loggable>();
        List<Loggable> loggables = new ArrayList<Loggable>();
        while (inputStream.read(loggables)) {
            tranx.addAll(loggables);
            loggables.clear();
        }

        // for debug
        for (Loggable loggable : tranx) {
            LOG.debug(((LogEntry) loggable).toString());
        }

        return true;
    }

    /**
     * ���������־�ļ���������log.$tranxId.$timestamp
     * ���Ը���$timestamp�������ҵ���ǰ���µ�һ����־�ļ�
     *
     * @return�ҵ���ǰ��־�ļ��� $timestamp�����Ǹ��ļ�����Ϊ��ǰ���µ�active����־�ļ�
     */
    private File findActiveLog() {
        File parent = new File(root, BASE_DIR);
        File tranxDir = new File(parent, tranxName);

        File activeLog = null;
        long latestIndex = Long.MIN_VALUE;

        for (File log : tranxDir.listFiles()) {

            String fileName = log.getName();
            String[] parts = fileName.split(FILE_NAME_SEPERATOR_REX);

            if (parts == null || parts.length != 3) {
                LOG.warn("[findActiveLog] -- invalid log file name {}", fileName);
                continue;
            }

            long index = Long.parseLong(parts[1]);
            if (index >= latestIndex) {
                latestIndex = index;
                activeLog = log;
            }
        }

        return activeLog;
    }

    private void delete(File file) {
        file.delete();
    }

    /**
     * @param loggables
     */
    private void innerLog(List<Loggable> loggables) {
        for (Loggable loggable : loggables) {
            synchronized (this) {   // �����ܼ�С�ٽ�����
                outputStream.write(loggable);
            }
        }
    }

    private synchronized void checkLogFull() {
        if (tranxId.get() % LOG_MAX_TRANX_NUM == 0) {
            try {
                File logFile = create();
                outputStream = new TranxLogFileOutputStream(logFile); //Ҫ��֤��ȫ����
                LOG.debug("log to new file.");
            } catch (IOException e) {
                LOG.error("create log file failed", e);
            }
        }
    }

    /**
     * ��logDir����һ���µ�log�ļ�, ���ڴ�����־�ļ���������
     * a)��ʼ״̬��
     * b)ǰһ����־�ļ�д������һ���ļ���д����LOG_MAX_TRANX_NUM��¼
     * ���Դ����ļ�ʱtranxIdҪ��������:tranxId.get() % LOG_MAX_TRANX_NUM == 0
     *
     * @return
     * @throws IOException
     */
    private File create() throws IOException {
        // ȷ����־Ŀ¼����
        File parent = new File(root, BASE_DIR);
        File tranxDir = new File(parent, tranxName);
        if (!tranxDir.exists()) {
            tranxDir.mkdirs();
        }

        if (tranxId.get() % LOG_MAX_TRANX_NUM != 0) {
            LOG.error("illegal log file index {}", tranxId.get());
            throw new IllegalStateException("illegal tranxId, when create log file");
        }

        File logFile = new File(tranxDir, buildLogName(tranxId.get() / LOG_MAX_TRANX_NUM));
        logFile.createNewFile();
        LOG.info("create new log file index: {}", tranxId.get() / LOG_MAX_TRANX_NUM);
        return logFile;
    }

    private String buildLogName(long index) {
        StringBuilder sb = new StringBuilder();
        sb.append("log");
        sb.append(FILE_NAME_SEPERATOR);
        sb.append(index);
        sb.append(FILE_NAME_SEPERATOR);
        sb.append(System.currentTimeMillis());
        return sb.toString();
    }

    private static class TranxLogFileOutputStream {
        private File file;
        private FileOutputStream fos;
        private DataOutputBuffer buffer = new DataOutputBuffer();

        private TranxLogFileOutputStream(File file) throws FileNotFoundException {
            this.file = file;
            fos = new FileOutputStream(file, true); // append mode
        }

        public void write(Loggable loggable) {
            buffer.writeLoggable(loggable);
            try {
                buffer.writeUTF8(LogEntry.LOG_ENTRY_SEPRATOR);
                byte[] t = LogEntry.LOG_ENTRY_SEPRATOR.getBytes(LogEntry.DEFAULT_ENCODE);
            } catch (IOException e) {
            }
        }

        public void flushAndSync() throws IOException {
            // write and flush
            fos.write(buffer.toByteArray());
            fos.flush();
            // sync to disk
            FileDescriptor fd = fos.getFD();
            fd.sync();
            // reset the buffer for reuse
            buffer.reset();
        }

        public void close() throws IOException {
            flushAndSync();
            fos.close();
        }
    }

    private static class TranxLogFileInputStream {
        private RandomAccessFile ris;
        private int steps;
        private long chunkSize;
        private long lastStartPos;

        private TranxLogFileInputStream(File file, int steps) throws IOException {
            this.ris = new RandomAccessFile(file, "r");
            this.steps = steps;
            chunkSize = ris.length() / (long) steps;
            lastStartPos = ris.length();
        }

        /**
         * ���ļ�ĩβ��ʼ�������ȡһ��loggable��¼
         * ��steps�����ּ�����ȡһ����־�ļ���steps������һ�������ļ���С��
         * <p/>
         * ���̰߳�ȫ
         *
         * @param loggables
         * @throws IOException
         */
        public boolean read(List<Loggable> loggables) throws IOException {
            if (steps <= 0) return false;

            ris.seek((steps-- - 1) * chunkSize);
            if (ris.getFilePointer() > 0) ris.readLine(); // skip loggable fragment

            long startPos = ris.getFilePointer(); // 1st valid line position
            while (true) {
                if (ris.getFilePointer() >= lastStartPos) break; //

                String line = ris.readLine();
                if (line == null) break;

                DataInputBuffer buffer = new DataInputBuffer(line.getBytes());
                Loggable loggable = new LogEntry();
                buffer.readLoggable(loggable);
                loggables.add(loggable);
            }
            lastStartPos = startPos;

            return true;
        }

        /**
         * @throws IOException
         */
        public void close() throws IOException {
            ris.close();
        }
    }

}
