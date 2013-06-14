package com.taobao.joey.tranx;

import java.io.IOException;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: 下午3:43
 */
public class LogEntry implements Loggable {
    public static final String DEFAULT_ENCODE = "UTF-8";
    public static final byte OP_START_TRANX = 0x00;
    public static final byte OP_COMMIT_TRANX = 0X01;
    public static final String LOG_ENTRY_SEPRATOR = "\r\n";
    /**
     *
     */
    protected byte opCode;
    protected long timeStamp = System.currentTimeMillis();
    protected String contents;

    public LogEntry() {
    }

    public LogEntry(byte opCode, String contents) {
        this.opCode = opCode;
        this.contents = contents;
    }

    public boolean writeToLog(DataOutputBuffer buffer) {
        try {
            buffer.writeByte(opCode);
            buffer.writeLong(timeStamp);
            buffer.writeUTF8(contents);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean readFromLog(DataInputBuffer buffer) {
        try {
            opCode = buffer.readByte();
            timeStamp = buffer.readLong();
            if (buffer.available() > 0)
                contents = buffer.readUTF8();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (this == obj) return true;

        if (!(obj instanceof LogEntry)) return false;

        LogEntry entry = (LogEntry) obj;
        return entry.opCode == this.opCode
                && entry.timeStamp == this.timeStamp
                && entry.contents.equals(this.contents);
    }

    @Override
    public int hashCode() {
        int result = 17; // 素数
        // 选31 作为乘数是有说法的
        result = 31 * result + (int) opCode;
        result = 31 * result + Float.floatToIntBits(timeStamp);
        if (contents != null) {
            result = 31 * result + contents.hashCode();
        } else {
            result = 31 * result + 0;
        }
        return result;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "opCode=" + opCode +
                ", timeStamp=" + timeStamp +
                ", contents='" + contents + '\'' +
                '}';
    }

    public static class StartTranxLogEntry extends LogEntry {
        private StartTranxLogEntry(long tranxId) {
            super(OP_START_TRANX, String.valueOf(tranxId));
        }

        public static LogEntry newEntry(long tranxId) {
            return new StartTranxLogEntry(tranxId);
        }
    }

    public static class CommitTranxLogEntry extends LogEntry {
        public CommitTranxLogEntry(long tranxId) {
            super(OP_COMMIT_TRANX, String.valueOf(tranxId));
        }

        public static LogEntry newEntry(long tranxId) {
            return new CommitTranxLogEntry(tranxId);
        }
    }

}
