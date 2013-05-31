package com.taobao.joey.buffer;

import java.io.DataOutputStream;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * User: qiaoyi.dingqy
 * Date: 13-5-10
 * Time: ����3:56
 * <p/>
 * ʵ��DataOutput�ӿڵ��ڴ�OutputBuffer������д�ļ��������������������Wrapper Pattern
 * ע�⣺OutputBuffer��ʵ����ByteArrayOutputStream�ķ�װ�����ܼ���һ����ֻ�Ǳ�¶��ByteArrayOutputStream�е�byte��������
 */
public class DataOutputBuffer extends DataOutputStream {

    private OutputBuffer buffer;

    public DataOutputBuffer(OutputBuffer buffer) {
        super(buffer);
    }

    /**
     * Returns the current contents of the buffer.
     * Data is only valid to {@link #getLength()}.
     */
    public byte[] getData() {
        return buffer.getData();
    }

    /**
     * Returns the length of the valid data currently in the buffer.
     */
    public int getLength() {
        return buffer.getLength();
    }

    /**
     * Resets the buffer to empty.
     */
    public DataOutputBuffer reset() {
        this.written = 0;
        buffer.reset();
        return this;
    }
}
