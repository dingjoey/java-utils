package com.taobao.joey.buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: qiaoyi.dingqy
 * Date: 13-5-10
 * Time: 下午3:48
 * <p/>
 * OutputBuffer其实就是ByteArrayOutputStream的封装，功能几乎一样，只是暴露了ByteArrayOutputStream中的byte【】引用
 */
public class OutputBuffer extends ByteArrayOutputStream {

    public byte[] getData() {
        return buf;
    }

    public int getLength() {
        return count;
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param b the byte to be written.
     */
    @Override
    public synchronized void write(int b) {
        super.write(b);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) {
        super.write(b, off, len);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Resets the <code>count</code> field of this byte array output
     * stream to zero, so that all currently accumulated output in the
     * output stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space.
     *
     * @see java.io.ByteArrayInputStream#count
     */
    @Override
    public synchronized void reset() {
        super.reset();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number
     *         of valid bytes in this output stream.
     * @see java.io.ByteArrayOutputStream#count
     */
    @Override
    public synchronized int size() {
        return super.size();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Writes the complete contents of this byte array output stream to
     * the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param out the output stream to which to write the data.
     * @throws java.io.IOException if an I/O error occurs.
     */
    @Override
    public synchronized void writeTo(OutputStream out) throws IOException {
        super.writeTo(out);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
