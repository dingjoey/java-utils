package com.taobao.joey.wordcnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-8-19
 * Time: 下午5:05
 */
public class WC9 {
    //
    static byte[] punctuation
            = {':', '.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
    static HashSet<String> stopWords
            = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));
    //
    static int top = 10;
    static int candidateSize = stopWords.size() + 10;
    // 读文件用
    static int PAGE_SIZE = 65535;
    static int chunkSize = PAGE_SIZE;
    static File file;
    static long fileLength;
    static byte[] chunk;
    static int splitLength;
    // wordcnt runnable
    static int wcThreadNum = 67;
    static CountDownLatch wcRunnablelatch = new CountDownLatch(wcThreadNum);
    static Map<JString, MutableInt> wc = new ConcurrentHashMap<JString, MutableInt>(50000, 0.9f);
    // rank runnable
    static int rankThreadNum = 67;
    //static List<ConcurrentHashMap<JString, AtomicInteger>> splitWC = new ArrayList<ConcurrentHashMap<JString, AtomicInteger>>(rankThreadNum);
    static List<List<HashMap<JString, AtomicInteger>>> splitWCs = new ArrayList<List<HashMap<JString, AtomicInteger>>>(wcThreadNum);
    static JString[][][] splitRankArray;
    static CountDownLatch rankRunnablelatch = new CountDownLatch(rankThreadNum);
    // merge runnable
    // static WCResult[][] result = new WCResult[rankThreadNum][top];
    static WCResult[][] result = new WCResult[rankThreadNum][];

    static {
        try {
            init(16, 16, 10);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    static void init(int wn, int rn, int t) throws URISyntaxException {
        long start = System.currentTimeMillis();
        file = new File(WC9.class.getResource("/document.txt").toURI());
        fileLength = file.length();
        chunk = new byte[(int) fileLength];
        splitLength = chunk.length / wcThreadNum;
        for (int i = 0; i < wcThreadNum; i++) {
            List<HashMap<JString, AtomicInteger>> splitWC = new ArrayList<HashMap<JString, AtomicInteger>>(rankThreadNum);
            for (int j = 0; j < rankThreadNum; j++) {
                splitWC.add(new HashMap<JString, AtomicInteger>(50000, 0.9f));
            }
            splitWCs.add(splitWC);
        }

        long end = System.currentTimeMillis();
        System.out.println("init consumes : " + (end - start));
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        long startReadFile = System.currentTimeMillis();
        mmapRead();
        long endReadFile = System.currentTimeMillis();

        long startWordCnt = System.currentTimeMillis();
        Thread[] threads1 = new Thread[wcThreadNum];
        for (int i = 0; i < wcThreadNum; i++) {
            threads1[i] = new Thread(new WordCntRunnable(i));
            threads1[i].start();
        }
        wcRunnablelatch.await();
        long endWordCnt = System.currentTimeMillis();

        long startTopTen = System.currentTimeMillis();
        Thread[] threads2 = new Thread[wcThreadNum];
        for (int i = 0; i < wcThreadNum; i++) {
            threads2[i] = new Thread(new RankRunnable(i));
            threads2[i].start();
        }
        rankRunnablelatch.await();

        merge();
        long endTopTen = System.currentTimeMillis();


        System.out.println("read file consumes: " + (endReadFile - startReadFile) + "ms");
        System.out.println("word cnt consumes: " + (endWordCnt - startWordCnt) + "ms");
        System.out.println("rank consumes: " + (endTopTen - startTopTen) + "ms");
        System.out.println("total consumes: " + (endTopTen - startReadFile) + "ms");
    }

    static void mmapRead() throws IOException, URISyntaxException {
        MappedByteBuffer buffer = new RandomAccessFile(file, "rw").getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, file.length());

        for (int i = 0; i < chunk.length && buffer.hasRemaining(); ) {
            int readLength = chunkSize > buffer.remaining() ? buffer.remaining() : chunkSize;
            buffer.get(chunk, i, readLength);
            i += readLength;
        }
    }

    static void wordCnt(int id, int startIndex, int endIndex, Map<JString, MutableInt> wordCnt) {
        long start = System.currentTimeMillis();
        int wordStartIndex = -1;
        int wordEndIndex = -1;
        JString first = null;
        JString word = null;
        for (int i = startIndex; i <= endIndex; i++) {
            if (!isSplitter(chunk[i])) {
                if (wordStartIndex == -1) {
                    wordStartIndex = i;
                    wordEndIndex = wordStartIndex;
                } else {
                    wordEndIndex++;
                }
            } else {
                if (wordStartIndex != -1) {
                    // 一次遍历完成：1) to lower case; 2) compute hashcode
                    int hashcode = 0;
                    for (int j = wordStartIndex; j < wordEndIndex + 1; ++j) {
                        byte c = chunk[j];
                        if (c >= 'A' && c <= 'Z') {
                            chunk[j] = (byte) (c + 32);
                        }
                        hashcode = hashcode * 31 + chunk[j];
                    }

                    // 避免copy,构造JString对象作为计数的key
                    word = new JString(wordStartIndex, wordEndIndex, hashcode);
                    // mutableInt 一次计数只访问一次map
                    int index = hashcode % splitWCs.get(id).size();
                    HashMap<JString, AtomicInteger> splitWordCnt = splitWCs.get(id).get(index > 0 ? index : 0 - index);
                    AtomicInteger cnt = splitWordCnt.get(word);
                    if (cnt != null) cnt.getAndIncrement();
                    else splitWordCnt.put(word, new AtomicInteger(1));
                    wordStartIndex = -1;
                }
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("word cnt consumes : " + (end - start));
    }

    static boolean isSplitter(byte c) {
        for (byte p : punctuation) {
            if (p == c) return true;
        }
        return false;
    }

    static boolean isStopWords(JString word) {
        if (word.len > 6) {// stopword 特征
            return false;
        }
        return stopWords.contains(word.toString());
    }

    static void merge() {
        ArrayList<WCResult> topTen = new ArrayList<WCResult>(16);
        int index[] = new int[rankThreadNum];

        for (int i = 0; i < 10; i++) {
            WCResult max = null;
            JString top = null;
            int maxIndex = -1;
            for (int j = 0; j < rankThreadNum; j++) {
                if (index[j] > 10) continue;
                WCResult r = result[j][index[j]];
                if (max == null || max.cnt < r.cnt) {
                    max = r;
                    maxIndex = j;
                }

            }
            index[maxIndex]++;
            topTen.add(max);
        }

        for (WCResult word : topTen) {
            System.out.println(word);
        }
    }

    static class MutableInt {
        AtomicInteger value = new AtomicInteger(1);

        void inc() {
            value.getAndIncrement();
        }

        int get() {
            return value.get();
        }
    }

    static class WCResult {
        final JString word;
        final int cnt;

        WCResult(JString word, int cnt) {
            this.word = word;
            this.cnt = cnt;
        }

        @Override
        public String toString() {
            return "WCResult{" +
                    "word=" + word +
                    ", cnt=" + cnt +
                    '}';
        }
    }

    static class JString {
        //
        int start;
        int end;
        int hashcode;
        int len;

        JString(int start, int end, int hashcode) {
            this.start = start;
            this.end = end;
            this.hashcode = hashcode;
            len = end - start + 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JString)) return false;

            JString jString = (JString) o;
            if (this.start == jString.start) return true;

            if (this.len != jString.len) return false;
            for (int i = 0; i < len; i++) {
                if (chunk[this.start + i] != chunk[jString.start + i]) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return this.hashcode;
        }

        @Override
        public String toString() {
            return new String(chunk, start, len);
        }
    }

    static class WordCntRunnable implements Runnable {
        final int id;

        WordCntRunnable(int id) {
            this.id = id;
        }

        public void run() {
            int startIndex = id * splitLength;
            if (startIndex != 0) {
                while (!isSplitter(chunk[startIndex]) && !isSplitter(chunk[startIndex - 1])) {
                    startIndex++;
                }
            }

            int endIndex = (id + 1) * splitLength - 1;
            while (!isSplitter(chunk[endIndex])) {
                endIndex++;
            }

            if (id == wcThreadNum - 1) {
                endIndex = chunk.length - 1;
            }

            wordCnt(id, startIndex, endIndex, wc);
            wcRunnablelatch.countDown();
        }
    }

    static class RankRunnable implements Runnable {
        final int id;
        final Map<JString, AtomicInteger> split;
        final WCResult[] rank;

        RankRunnable(int id) {
            this.id = id;
            split = splitWCs.get(0).get(id);
            for (int i = 1; i < splitWCs.size(); i++){
                split.putAll(splitWCs.get(i).get(id));
            }
            rank = new WCResult[split.entrySet().size()];
            result[id] = rank;

        }

        public void run() {
            int len = 0;
            int top = 10;
            for (Map.Entry<JString, AtomicInteger> entry : split.entrySet()) {
                int cnt = entry.getValue().get();
                JString word = entry.getKey();
                //
                if (cnt == 1) continue;

                if (!isStopWords(word)) {
                    int i = 0;
                    for (; i < len; i++) {
                        if (rank[i] == null || cnt > rank[i].cnt) break;
                    }

                    if (len >= top && i == len) continue;
                    else {
                        if (rank[i] != null) {
                            for (int j = len - 1; j >= i + 1; j--) {
                                rank[j] = rank[j - 1];
                            }
                        }
                        rank[i] = new WCResult(word, cnt);
                        len++;
                    }
                }
            }

            rankRunnablelatch.countDown();
        }
    }
}
