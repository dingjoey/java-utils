package com.taobao.joey.tranx;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-5
 * Time: ÏÂÎç3:03
 */
public class DataOutputBufferTest {

    @Test
    public void testPrimitive() throws IOException {
        DataOutputBuffer outputBuffer = new DataOutputBuffer();
        byte wb = (byte) 0xFF;
        String wStr = "¹þ¹þxixi£¡£¿£¤";
        long wl = 90l;
        outputBuffer.writeByte(wb);
        outputBuffer.writeLong(wl);
        outputBuffer.writeUTF8(wStr);
        System.out.println(outputBuffer.dump("UTF-8"));

        DataInputBuffer inputBuffer = new DataInputBuffer(outputBuffer.toByteArray());
        byte rb = inputBuffer.readByte();
        long rl = inputBuffer.readLong();
        String rStr = inputBuffer.readUTF8();
        System.out.println(Integer.toHexString(rb));
        System.out.println(Long.toHexString(rl));
        System.out.println(rStr);


        Assert.assertEquals(wb, rb);
        Assert.assertEquals(wl, rl);
        Assert.assertEquals(wStr, rStr);
    }

    @Test
    public void testLoogable() {
        DataOutputBuffer outputBuffer = new DataOutputBuffer();
        Loggable wLoggable = LogEntry.StartTranxLogEntry.newEntry(33l);
        outputBuffer.writeLoggable(wLoggable);

        DataInputBuffer inputBuffer = new DataInputBuffer(outputBuffer.toByteArray());
        Loggable rLoggable = new LogEntry();
        inputBuffer.readLoggable(rLoggable);

        System.out.println(rLoggable.toString());
        System.out.println(wLoggable.toString());

        Assert.assertEquals(wLoggable, rLoggable);
    }
}
