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
    private static final String LOG_ENTRY_LINE_SEPRATOR = "\r\n";
    private byte opCode;
    private String contents;

    public LogEntry() {
    }

    public LogEntry(byte opCode, String contents) {
        this.opCode = opCode;
        this.contents = contents;
    }

    public void writeToLog(DataOutputBuffer buffer) {
        try {
            buffer.writeByte(opCode);
            buffer.writeUTF8(contents);
            buffer.writeUTF8(LOG_ENTRY_LINE_SEPRATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readFromLog(DataInputBuffer buffer) {
        try {
            opCode = buffer.readByte();
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

        return entry.opCode == this.opCode && entry.contents.equals(this.contents);
    }

    @Override
    public int hashCode() {
        int result = 17; // ËØÊý
        result = 31 * result + (int) opCode;
        if (contents != null) {
            result = 31 * result + contents.hashCode();
        } else {
            result = 31 * result + 0;
        }
        return result;
    }
}
