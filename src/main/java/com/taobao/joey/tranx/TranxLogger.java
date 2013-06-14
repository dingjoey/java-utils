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
 * Time: 下午3:53
 * <p/>
 * <p/>
 * 每个需要进行操作持久化的业务场景创建一个TranxLogger实例
 * tranxName为对应业务场景的名字,其也定义了用于保证持久性的日志文件的目录名字
 */
public class TranxLogger implements ITranxLogger {
    /**
     * 事务id，有效范围
     * 1-9223372036854775807
     */
    public static final AtomicLong tranxId = new AtomicLong(0l);
    private static final Logger LOG = LoggerFactory.getLogger(TranxLogger.class);
    /**
     * 日志文件路径
     * $root/transaction-log/$tranxName/log.$tranxId.$timestamp
     */
    private static final String BASE_DIR = "transaction-log";
    private static final String FILE_NAME_SEPERATOR = ".";
    private static final String FILE_NAME_SEPERATOR_REX = "\\.";
    private static final String DEFAULT_TRANX_NAME = "default-transaction";
    /**
     * 定义一个日志文件大小的上限为记录LOG_MAX_TRANX_NUM个事务
     */
    private static final int LOG_MAX_TRANX_NUM = 1000;
    /**
     * 用作TranxLogger实例的缓存 key == root + FILE_NAME_SEPERATOR +  tranxName
     */
    private static final Map<String, TranxLogger> tranxLoggers = new HashMap<String, TranxLogger>();
    private String root;
    private String tranxName;
    /**
     * 最新的一个事务日志记录sync到磁盘的的transaction id
     */
    private volatile long syncTranxId = Long.MIN_VALUE;
    /**
     * 日志文件输出流
     */
    private volatile TranxLogFileOutputStream outputStream;  //AtomicReference作用一样，但是更易读
    /**
     * 日志文件输入流
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
     * 扫描日志目录下的所有目录文件，定位日志尾，打开写入流
     *
     * @return
     */
    final public synchronized boolean openLogging() {
        // 找到日志文件尾;打开输出流
        File activeLog = findActiveLog();
        if (activeLog == null) {  // 没有日志文件则创建一个新的
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
     * 关闭写入流，并且保证buffer sync to disk
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
        // 确保日志目录存在
        File parent = new File(root, BASE_DIR);
        File tranxDir = new File(parent, tranxName);
        if (!tranxDir.exists()) {
            return false;
        }

        // 找到日志文件尾;打开输入流
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
     * 事务操作日志文件命名规则log.$tranxId.$timestamp
     * 所以根据$timestamp，可以找到当前最新的一个日志文件
     *
     * @return找到当前日志文件中 $timestamp最大的那个文件，作为当前最新的active的日志文件
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
            synchronized (this) {   // 尽可能减小临界区域
                outputStream.write(loggable);
            }
        }
    }

    private synchronized void checkLogFull() {
        if (tranxId.get() % LOG_MAX_TRANX_NUM == 0) {
            try {
                File logFile = create();
                outputStream = new TranxLogFileOutputStream(logFile); //要保证安全发布
                LOG.debug("log to new file.");
            } catch (IOException e) {
                LOG.error("create log file failed", e);
            }
        }
    }

    /**
     * 在logDir创建一个新的log文件, 由于创建日志文件的条件：
     * a)初始状态下
     * b)前一个日志文件写满，即一个文件中写满了LOG_MAX_TRANX_NUM记录
     * 所以创建文件时tranxId要满足条件:tranxId.get() % LOG_MAX_TRANX_NUM == 0
     *
     * @return
     * @throws IOException
     */
    private File create() throws IOException {
        // 确保日志目录存在
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
         * 从文件末尾开始，逆序读取一批loggable记录
         * 由steps决定分几批读取一个日志文件，steps的设置一般依据文件大小定
         * <p/>
         * 非线程安全
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
