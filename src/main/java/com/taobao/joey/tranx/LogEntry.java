package com.taobao.joey.tranx;

import java.io.IOException;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: ÏÂÎç3:43
 */
public class LogEntry implements Loggable {
    private static final byte OP_START_TRANX = 0x00;
    private static final byte OP_COMMIT_TRANX = 0X01;
    private static final String LOG_ENTRY_LINE_SEPRATOR = "\r\n";
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

    public void writeToLog(DataOutputBuffer buffer) {
        try {
            buffer.writeByte(opCode);
            buffer.writeLong(timeStamp);
            buffer.writeUTF8(contents);
            buffer.writeUTF8(LOG_ENTRY_LINE_SEPRATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readFromLog(DataInputBuffer buffer) {
        try {
            opCode = buffer.readByte();
            timeStamp = buffer.readLong();
            contents = buffer.readUTF8();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        if (this == obj) return true;
        LogEntry entry = (LogEntry) obj;

        return entry.opCode == this.opCode
                && entry.timeStamp == this.timeStamp
                && entry.contents.equals(this.contents);
    }

    @Override
    public int hashCode() {
        int result = 17; // ËØÊý
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
        public StartTranxLogEntry() {
            super(OP_START_TRANX, null);
        }

        public StartTranxLogEntry(long tranxId) {
            super(OP_START_TRANX, String.valueOf(tranxId));
        }

        public StartTranxLogEntry(byte opCode, String contents) {
            throw new UnsupportedOperationException();
        }
    }

    public static class TranxCommitLogEntry extends LogEntry {
        public TranxCommitLogEntry() {
            super(OP_COMMIT_TRANX, null);
        }

        public TranxCommitLogEntry(long tranxId) {
            super(OP_COMMIT_TRANX, String.valueOf(tranxId));
        }

        public TranxCommitLogEntry(byte opCode, String contents) {
            throw new UnsupportedOperationException();
        }
    }

}
