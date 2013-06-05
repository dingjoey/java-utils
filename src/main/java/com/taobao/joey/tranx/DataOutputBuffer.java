package com.taobao.joey.tranx;

import java.io.*;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: 下午2:36
 * <p/>
 * 封装ByteArrayOutputStream（byte[]）实现一个DataOutput接口的子集
 */
public class DataOutputBuffer extends ByteArrayOutputStream {
    private DataOutputStream os;

    public DataOutputBuffer() {
        os = new DataOutputStream(this);
    }

    public void writeByte(byte b) throws IOException {
        os.writeByte(b);
    }

    public void writeUTF8(String str) throws IOException {
        os.writeUTF(str);
    }

    public void writeLoggable(Loggable loggable) {
        loggable.writeToLog(this);
    }

    public String dump(String charset) throws UnsupportedEncodingException {
        return toString(charset);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
