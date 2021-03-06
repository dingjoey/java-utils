package com.taobao.joey.wordcnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-8-18
 * Time: ����1:54
 */
public class WC7 {
    //
    static int PAGE_SIZE = 65535;
    static byte[] chunk;
    //
    static byte[] punctuation
            = {'.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
    //static Map<String, Integer> threadLocalWordCnt = new HashMap<String, Integer>(40000, 0.99f);
    //
    static List<JString> wordRank = new ArrayList<JString>(10);
    static List<Integer> cntRank = new ArrayList<Integer>(10);
    static HashSet<JString> stopWords
            = new HashSet<JString>(Arrays.asList(
            new JString("the".getBytes()),
            new JString("and".getBytes()),
            new JString("i".getBytes()),
            new JString("to".getBytes()),
            new JString("of".getBytes()),
            new JString("a".getBytes()),
            new JString("in".getBytes()),
            new JString("was".getBytes()),
            new JString("that".getBytes()),
            new JString("had".getBytes()),
            new JString("he".getBytes()),
            new JString("you".getBytes()),
            new JString("his".getBytes()),
            new JString("my".getBytes()),
            new JString("it".getBytes()),
            new JString("as".getBytes()),
            new JString("with".getBytes()),
            new JString("her".getBytes()),
            new JString("for".getBytes()),
            new JString("on".getBytes())
            ));
    //
    static long fileLength;
    static int splitLength;
    //
    static int wordCntThreadsNum = 8;
    static CountDownLatch latch = new CountDownLatch(wordCntThreadsNum);
    static List<HashMap<JString, MutableInt>> threadLocalWordCntMutableInt = new ArrayList<HashMap<JString, MutableInt>>(wordCntThreadsNum);

    static {
        for (int i = 0; i < wordCntThreadsNum; i++) {
            threadLocalWordCntMutableInt.add(new HashMap<JString, MutableInt>());
        }
    }
    static class MutableInt {
        int value = 1;
        void inc() { ++value; }
        int get() { return value; }
    }
    static class JString{
        byte[] value;

        JString(byte[] value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JString)) return false;

            JString jString = (JString) o;

            if (!Arrays.equals(value, jString.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        @Override
        public String toString() {
            return new String(value);
        }
    }


    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        if (args.length > 0) {
            wordCntThreadsNum = Integer.parseInt(args[0]);
            latch = new CountDownLatch(wordCntThreadsNum);

            threadLocalWordCntMutableInt =  new ArrayList<HashMap<JString, MutableInt>>(wordCntThreadsNum);
            for (int i = 0; i < wordCntThreadsNum; i++) {
                threadLocalWordCntMutableInt.add(new HashMap<JString, MutableInt>());
            }
            System.out.println("using threads : " + wordCntThreadsNum);
        }

        long startReadFile = System.currentTimeMillis();
        mmapRead();
        long endReadFile = System.currentTimeMillis();

        long startWordCnt = System.currentTimeMillis();
        splitLength = chunk.length / wordCntThreadsNum;
        Thread[] threads = new Thread[wordCntThreadsNum];
        for (int i = 0; i < wordCntThreadsNum; i++) {
            threads[i] = new Thread(new WordCntRunnable(i));
            threads[i].start();
        }
        latch.await();
        long endWordCnt = System.currentTimeMillis();

        long startTopTen = System.currentTimeMillis();
        topTenUsingMutableInt();
        long endTopTen = System.currentTimeMillis();

        System.out.println("read file consumes: " + (endReadFile - startReadFile) + "ms");
        System.out.println("word cnt consumes: " + (endWordCnt - startWordCnt) + "ms");
        System.out.println("rank consumes: " + (endTopTen - startTopTen) + "ms");
        System.out.println("total consumes: " + (endTopTen - startReadFile) + "ms");
    }

    static void mmapRead() throws IOException, URISyntaxException {
        File file = new File(WC7.class.getResource("/document.txt").toURI());

        MappedByteBuffer buffer = new RandomAccessFile(file, "rw").getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, file.length());

        fileLength = file.length();
        chunk = new byte[(int) fileLength];
        int chunkSize = PAGE_SIZE;
        for (int i = 0; i < chunk.length && buffer.hasRemaining(); ) {
            int readLength = chunkSize > buffer.remaining() ? buffer.remaining() : chunkSize;
            buffer.get(chunk, i, readLength);
            i += readLength;
        }

    }

    /**
     * 1. word split
     * 2. to lower case
     * 3. count
     */
    static void wordCntUsingMutableInt(int startIndex, int endIndex, Map<JString, MutableInt> wordCnt) {
        long start = System.currentTimeMillis();
        int wordStartIndex = -1;
        int wordEndIndex = -1;
        for (int i = startIndex; i < endIndex; i++) {
            if (!isSplitter(chunk[i])) {
                if (wordStartIndex == -1) {
                    wordStartIndex = i;
                    wordEndIndex = wordStartIndex;
                } else {
                    wordEndIndex++;
                }
            } else {
                if (wordStartIndex != -1) {
                    int len = wordEndIndex + 1 - wordStartIndex;
                    byte[] wordByte = new byte[len];
                    for(int j = 0; j < len; ++j){
                        byte c = chunk[wordStartIndex + j];
                        if(c>='A' && c<='Z'){
                            wordByte[j] = (byte)(c +32);
                        } else{
                            wordByte[j] = c;
                        }
                    }
                    JString word = new JString(wordByte);
                    MutableInt cnt = wordCnt.get(word);
                    if (cnt == null) {
                        wordCnt.put(word, new MutableInt());
                    } else {
                        cnt.inc();
                    }
                    wordStartIndex = -1;
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("word cnt consumes : " + (end - start));
    }

    static void topTenUsingMutableInt() {
        // TODO bugs
        for (Map.Entry<JString, MutableInt> entry : threadLocalWordCntMutableInt.get(0).entrySet()) {
            JString word = entry.getKey();
            MutableInt cnt = entry.getValue();

            for (int i = 1; i < threadLocalWordCntMutableInt.size(); i++) {
                MutableInt c = threadLocalWordCntMutableInt.get(i).get(word);
                if (c != null) cnt.value += c.value;
            }

            int i = 0;
            for (; i < cntRank.size(); i++) {
                if (cnt.value > cntRank.get(i)) break;
            }
            cntRank.add(i, cnt.value);
            wordRank.add(i, word);
        }

        int hit = 0;
        for (int i = 0; i < cntRank.size() && hit < 10; i++) {
            JString word = wordRank.get(i);
            if (stopWords.contains(word)) continue;
            System.out.println(word + ":" + cntRank.get(i));
            hit++;
        }
    }

    static boolean isSplitter(byte c) {
        for (byte p : punctuation) {
            if (p == c) return true;
        }
        return false;
    }

    static class WordCntRunnable implements Runnable {
        final int id;
        final Map<JString, MutableInt> wordCntMutableInt;

        WordCntRunnable(int id) {
            this.id = id;
            wordCntMutableInt = threadLocalWordCntMutableInt.get(id);
        }

        public void run() {
            int startIndex = id * splitLength;
            if (startIndex != 0) {
                while (!isSplitter(chunk[startIndex])) {
                    startIndex++;
                }
            }

            int endIndex = (id + 1) * splitLength - 1;
            while (!isSplitter(chunk[endIndex])) {
                endIndex++;
            }

            wordCntUsingMutableInt(startIndex,endIndex,wordCntMutableInt);
            latch.countDown();
        }
    }
}
