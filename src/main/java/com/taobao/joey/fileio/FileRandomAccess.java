package com.taobao.joey.fileio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * User: qiaoyi.dingqy
 * Date: 13-5-13
 * Time: ����1:17
 * <p/>
 * ����RandomAccessFile seek�� FileChannel��position��Ч��һ�����������ô���ͬһ��Ч����Χ
 */
public class FileRandomAccess {


    /**
     *
     * ����RandomAccessFile seek�� FileChannel��position��Ч��һ�����������ô���ͬһ��Ч����Χ
     *
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        File file = new File("./test");
        file.createNewFile();
        RandomAccessFile ra = new RandomAccessFile(file, "rw");
        FileChannel channel = ra.getChannel();

        ra.seek(10);
        ra.writeUTF("1");


        System.out.println(channel.position());
        System.out.println(ra.getFilePointer());

        channel.position(0);
        ra.writeUTF("2");
        System.out.println(channel.position());
        System.out.println(ra.getFilePointer());

        channel.position(0);
        System.out.println(ra.readUTF());
        ra.seek(10);
        System.out.println(ra.readUTF());
    }
}
