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
 * Time: ÏÂÎç1:54
 */
public class WC2 {
    //
    static int PAGE_SIZE = 65535;
    static byte[] chunk;
    //
    static byte[] punctuation
            = {'.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
    //static Map<String, Integer> threadLocalWordCnt = new HashMap<String, Integer>(40000, 0.99f);
    //
    static List<String> wordRank = new ArrayList<String>(10);
    static List<Integer> cntRank = new ArrayList<Integer>(10);
    static HashSet<String> stopWords
            = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));
    //
    static long fileLength;
    static int splitLength;
    //
    static int wordCntThreadsNum = 8;
    static CountDownLatch latch = new CountDownLatch(wordCntThreadsNum);
    static HashMap<Integer, HashMap<String, Integer>> threadLocalWordCnt = new HashMap<Integer, HashMap<String, Integer>>(wordCntThreadsNum);

    static {
        for (int i = 0; i < wordCntThreadsNum; i++) {
            threadLocalWordCnt.put(i, new HashMap<String, Integer>());
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
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
        topTen();
        long endTopTen = System.currentTimeMillis();

        System.out.println("read file consumes: " + (endReadFile - startReadFile) + "ms");
        System.out.println("word cnt consumes: " + (endWordCnt - startWordCnt) + "ms");
        System.out.println("rank consumes: " + (endTopTen - startTopTen) + "ms");
    }

    static void mmapRead() throws IOException, URISyntaxException {
        File file = new File(WC2.class.getResource("/document.txt").toURI());

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

        //System.out.println(new String(chunk));
    }

    /**
     * 1. word split
     * 2. to lower case
     * 3. count
     */
    static void wordCnt(int startIndex, int endIndex, Map<String, Integer> wordCnt) {
        long start = System.currentTimeMillis();
        String all = new String(chunk, startIndex, endIndex - startIndex);
        long end = System.currentTimeMillis();
        System.out.println("create word string consumes : " + (end - start));

        start = System.currentTimeMillis();
        String[] words = all.split("\\W+");
        end = System.currentTimeMillis();
        System.out.println("split word consumes : " + (end - start));

        start = System.currentTimeMillis();
        for (String word : words) {
            String lower = word.toLowerCase();
            Integer cnt = wordCnt.get(lower);
            if (cnt == null) {
                wordCnt.put(lower, 1);
            } else {
                wordCnt.put(lower, ++cnt);
            }
        }
        end = System.currentTimeMillis();
        System.out.println("word cnt consumes : " + (end - start));
    }

    static void wordCnt1(int startIndex, int endIndex, Map<String, Integer> wordCnt) {
        ArrayList<String> words = new ArrayList<String>(100000);

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
                    //byte[] wordByte = Arrays.copyOfRange(chunk, wordStartIndex, wordEndIndex + 1);
                    //String word = new String(wordByte).toLowerCase();
                    //words.add(word);
                    wordStartIndex = -1;
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("split word consumes : " + (end - start));

        start = System.currentTimeMillis();
        for (String word : words) {
            String lower = word.toLowerCase();
            Integer cnt = wordCnt.get(lower);
            if (cnt == null) {
                wordCnt.put(lower, 1);
            } else {
                wordCnt.put(lower, ++cnt);
            }
        }
        end = System.currentTimeMillis();
        System.out.println("word cnt consumes : " + (end - start));
    }

    /**
     *
     */
    static void topTen() {
        for (Map.Entry<String, Integer> entry : threadLocalWordCnt.get(0).entrySet()) {
            String word = entry.getKey();
            int cnt = entry.getValue();

            for (int i = 1; i < threadLocalWordCnt.size(); i++) {
                Integer c = threadLocalWordCnt.get(i).get(word);
                if (c != null) cnt += c;
            }

            int i = 0;
            for (; i < cntRank.size(); i++) {
                if (cnt > cntRank.get(i)) break;
            }
            cntRank.add(i, cnt);
            wordRank.add(i, word);
        }

        int hit = 0;
        for (int i = 0; i < cntRank.size() && hit < 10; i++) {
            String word = wordRank.get(i);
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
        final Map<String, Integer> wordCnt;

        WordCntRunnable(int id) {
            this.id = id;
            wordCnt = threadLocalWordCnt.get(id);
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

            wordCnt(startIndex, endIndex, wordCnt);
            latch.countDown();
        }
    }
}
