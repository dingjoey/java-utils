package com.taobao.joey.tranx;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: 下午2:36
 * <p/>
 * 1）封装ByteArrayOutputStream（byte[]）
 * <p/>
 * 2）借助Jdk中已有的功能，实现以Java类型为单位往buffer中写内容
 *    通过组合一个DataoutputStream，使用DataOutput接口的子集功能来实现
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

    public void writeLong(long l) throws IOException {
        os.writeLong(l);
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
