package com.taobao.joey.buffer;

import java.io.DataOutputStream;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * User: qiaoyi.dingqy
 * Date: 13-5-10
 * Time: 下午3:56
 * <p/>
 * 实现DataOutput接口的内存OutputBuffer，用于写文件的输出缓冲区，利用了Wrapper Pattern
 * 注意：OutputBuffer其实就是ByteArrayOutputStream的封装，功能几乎一样，只是暴露了ByteArrayOutputStream中的byte【】引用
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
