package com.taobao.joey.tranx;

import java.io.EOFException;

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

    @Override
    public void writeToLog(DataOutputBuffer buffer) {
        buffer.writeByte(opCode);
        buffer.writeUTF8(contents);
        buffer.writeUTF8(LOG_ENTRY_LINE_SEPRATOR);
    }

    @Override
    public void readFromLog(DataInputBuffer buffer) {
        try {
            opCode = buffer.readByte();
        } catch (EOFException e) {
        }
        contents = buffer.readUTF8();
    }

}
