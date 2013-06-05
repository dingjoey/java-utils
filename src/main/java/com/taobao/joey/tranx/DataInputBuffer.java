package com.taobao.joey.tranx;

import java.io.*;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: 下午3:00
 */
public class DataInputBuffer extends ByteArrayInputStream {

    private DataInputStream is;

    public DataInputBuffer(byte[] buf) {
        super(buf);
        is = new DataInputStream(this);
    }

    public byte readByte() throws IOException {
        return is.readByte();
    }

    public long readLong() throws IOException {
        return is.readLong();
    }

    public String readUTF8() throws IOException {
        return is.readUTF();
    }

    /**
     * 将反序列化逻辑隔离到Loggable里面实现
     *
     * @param loggable
     */
    public void readLoggable(Loggable loggable) {
        loggable.readFromLog(this);
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
