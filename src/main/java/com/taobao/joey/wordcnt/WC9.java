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
 * Time: ����5:05
 */
public class WC9 {
    //
    static byte[] punctuation
            = {'.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
    static HashSet<String> stopWords
            = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));
    //
    static int top = 10;
    static int candidateSize = stopWords.size();
    // ���ļ���
    static int PAGE_SIZE = 65535;
    static byte[] chunk;
    static int chunkSize = PAGE_SIZE;
    static File file;
    static long fileLength;
    // wordcnt runnable
    static int wcThreadNum = 16;
    static int splitLength;
    static CountDownLatch wcRunnablelatch = new CountDownLatch(wcThreadNum);
    static Map<JString, MutableInt> wc = new ConcurrentHashMap<JString, MutableInt>(50000, 0.9f);
    // rank runnable
    static int rankThreadNum = 16;
    static List<ConcurrentHashMap<JString, MutableInt>> splitWC = new ArrayList<ConcurrentHashMap<JString, MutableInt>>(rankThreadNum);
    //static List<ConcurrentHashMap<JString, AtomicInteger>> splitWCAtomicInteger = new ArrayList<ConcurrentHashMap<JString, AtomicInteger>>(rankThreadNum);
    //static List<List<List<JString>>> splitRank = new ArrayList<List<List<JString>>>(rankThreadNum);
    static List<JString[][]> splitRankArray = new ArrayList<JString[][]>(rankThreadNum);
    static CountDownLatch rankRunnablelatch = new CountDownLatch(rankThreadNum);
    // merge runnable
    static JString[][] splitRankResult = new JString[rankThreadNum][top];
    static int[][] splitCntResult = new int[rankThreadNum][top];

    static {
        try {
            init(16, 16, 10);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    static void init(int wn, int rn, int t) throws URISyntaxException {
        long start = System.currentTimeMillis();
        //
        top = t;
        candidateSize = top + stopWords.size();
        //
        file = new File(WC9.class.getResource("/document.txt").toURI());
        fileLength = file.length();
        chunk = new byte[(int) fileLength];
        chunkSize = PAGE_SIZE;
        // word cnt runnable init
        wcThreadNum = wn;
        wcRunnablelatch = new CountDownLatch(wcThreadNum);

        // rank cnt runnable init
        rankThreadNum = rn;
        splitWC = new ArrayList<ConcurrentHashMap<JString, MutableInt>>(rankThreadNum);
        for (int i = 0; i < rankThreadNum; i++) {
            splitWC.add(new ConcurrentHashMap<JString, MutableInt>(50000, 0.9f));
        }
        /*
        rankThreadNum = rn;
        splitWCAtomicInteger = new ArrayList<ConcurrentHashMap<JString, AtomicInteger>>(rankThreadNum);
        for (int i = 0; i < rankThreadNum; i++) {
            splitWCAtomicInteger.add(new ConcurrentHashMap<JString, AtomicInteger>(50000, 0.9f));
        }
        */

        int avgWordLen = 5;
        int avgFreq = 10;
        int radixSortSize = (int) (fileLength / avgWordLen) / avgFreq;
        splitRankArray = new ArrayList<JString[][]>(rankThreadNum);
        for (int i = 0; i < rankThreadNum; i++) {
            // ���ڻ�������
            JString[][] rank = new JString[radixSortSize][candidateSize];
            //System.out.println("init consumes : " + (end - start));
            // lazy create
            splitRankArray.add(rank);
        }

        rankRunnablelatch = new CountDownLatch(rankThreadNum);
        long end = System.currentTimeMillis();
        System.out.println("init consumes : " + (end - start));
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        long startReadFile = System.currentTimeMillis();
        mmapRead();
        long endReadFile = System.currentTimeMillis();

        long startWordCnt = System.currentTimeMillis();
        splitLength = chunk.length / wcThreadNum;
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

    static void wordCnt(int startIndex, int endIndex, Map<JString, MutableInt> wordCnt) {
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
                    // һ�α�����ɣ�1) to lower case; 2) compute hashcode
                    int hashcode = 0;
                    for (int j = wordStartIndex; j < wordEndIndex + 1; ++j) {
                        byte c = chunk[j];
                        if (c >= 'A' && c <= 'Z') {
                            chunk[j] = (byte) (c + 32);
                        }
                        hashcode = hashcode * 31 + chunk[j];
                    }
                    // ����copy,����JString������Ϊ������key
                    JString word = new JString(wordStartIndex, wordEndIndex, hashcode);
                    // mutableInt һ�μ���ֻ����һ��map
                    int index = hashcode % splitWC.size();
                    ConcurrentHashMap<JString, MutableInt> splitWordCnt = splitWC.get(index > 0 ? index : 0 - index);
                    MutableInt cnt = splitWordCnt.putIfAbsent(word, new MutableInt());
                    if (cnt != null) cnt.inc();
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
        if (word.len > 6) {// stopword ����
            return false;
        }
        return stopWords.contains(word.toString());
    }

    static void merge() {
        ArrayList<JString> topTen = new ArrayList<JString>();
        int index[] = new int[rankThreadNum];

        for (int i = 0; i < 10; i++) {
            int max = 0;
            JString top = null;
            int maxIndex = -1;
            for (int j = 0; j < rankThreadNum; j++) {
                int cnt = splitCntResult[j][index[j]];
                if (max < cnt) {
                    max = cnt;
                    top = splitRankResult[j][index[j]];
                    maxIndex = j;
                }

            }
            index[maxIndex]++;
            topTen.add(top);
        }

        for (JString word : topTen) {
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
                while (!isSplitter(chunk[startIndex])) {
                    startIndex++;
                }
            }

            int endIndex = (id + 1) * splitLength - 1;
            while (!isSplitter(chunk[endIndex])) {
                endIndex++;
            }

            wordCnt(startIndex, endIndex, wc);
            wcRunnablelatch.countDown();
        }
    }

    static class RankRunnable implements Runnable {
        final int id;
        final Map<JString, MutableInt> split;
        final JString[][] rank;
        final int[] index;
        final JString[] result;
        final int[] cnt;

        RankRunnable(int id) {
            this.id = id;
            split = splitWC.get(id);
            rank = splitRankArray.get(id);
            index = new int[rank.length];
            result = splitRankResult[id];
            cnt = splitCntResult[id];
        }

        public void run() {
            int max = 0;
            for (Map.Entry<JString, MutableInt> entry : split.entrySet()) {
                int cnt = entry.getValue().get();
                int i = index[cnt];
                if (i >= candidateSize) continue;
                JString[] words = rank[cnt];
                words[index[cnt]++] = entry.getKey();
                if (cnt > max) max = cnt;
            }

            int c = 0;
            for (int i = max; i >= 0; i--) {
                JString[] words = rank[i];
                if (words == null) continue;
                for (int j = 0; j < index[i]; j++) {
                    JString word = words[j];
                    if (!isStopWords(word)) {
                        if (c >= top) break;
                        result[c] = word;
                        cnt[c] = i;
                        c++;
                    }
                }
            }
            rankRunnablelatch.countDown();
        }
    }
}
