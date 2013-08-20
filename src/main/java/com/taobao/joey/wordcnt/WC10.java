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
 * Date: 13-8-20
 * Time: 下午6:26
 */
public class WC10 {
    static byte[] punctuation
            = {':', '.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
    static HashSet<String> stopWords
            = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));
    //
    static FileChannel fileChannel;
    static File file;
    static int splitSize;
    static int fileLength;
    static MappedByteBuffer input;
    //
    static int emmitTaskRunnableNum = 4;
    static ArrayList<ArrayList<HashMap<String, MutableInt>>> emitTaskOutput = new ArrayList<ArrayList<HashMap<String, MutableInt>>>(emmitTaskRunnableNum);
    static CountDownLatch emmitTaskBarrier = new CountDownLatch(emmitTaskRunnableNum);
    //
    static int rankTaskRunnableNum = 4;
    static WCResult[][] rankTaskOutput = new WCResult[rankTaskRunnableNum][];
    static CountDownLatch rankTaskBarrier = new CountDownLatch(rankTaskRunnableNum);

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        // init
        init(4, 4);

        // emmit task
        long startEmitTask = System.currentTimeMillis();
        Thread[] threads1 = new Thread[emmitTaskRunnableNum];
        for (int i = 0; i < emmitTaskRunnableNum; i++) {
            threads1[i] = new Thread(new EmmitTaskRunnable(i));
            threads1[i].start();
        }
        emmitTaskBarrier.await();
        long endEmitTask = System.currentTimeMillis();
        System.out.println("emit task consumes : " + (endEmitTask - startEmitTask));


        rankTaskBarrier.await();
        long startRankTask = System.currentTimeMillis();
        Thread[] threads2 = new Thread[emmitTaskRunnableNum];
        for (int i = 0; i < emmitTaskRunnableNum; i++) {
            threads2[i] = new Thread(new RankTaskRunnable(i));
            threads2[i].start();
        }
        rankTaskBarrier.await();
        long endRankTask = System.currentTimeMillis();
        System.out.println("emit task consumes : " + (endRankTask - startRankTask));
        //

        merge();

    }

    public static void init(int en, int rn) throws URISyntaxException, IOException {
        // init emmit task thread num
        emmitTaskRunnableNum = en;
        // init emmit task input
        file = new File(WC10.class.getResource("/document.txt").toURI());
        fileChannel = new RandomAccessFile(file, "rw").getChannel();
        fileLength = (int) file.length();
        splitSize = fileLength / emmitTaskRunnableNum;

        // init emmit task barrier
        emmitTaskBarrier = new CountDownLatch(emmitTaskRunnableNum);

        //
        //init
        rankTaskRunnableNum = rn;
        //
        rankTaskOutput = new WCResult[rankTaskRunnableNum][];

        // init emmit task output && rank task intput
        for (int i = 0; i < emmitTaskRunnableNum; i++) {
            ArrayList<HashMap<String, MutableInt>> rankSplits = new ArrayList<HashMap<String, MutableInt>>(emmitTaskRunnableNum);
            for (int j = 0; j < rankTaskRunnableNum; j++) {
                rankSplits.add(new HashMap<String, MutableInt>(10000, 0.9f)); // 4W / rankTaskRunnableNum
            }
            emitTaskOutput.add(rankSplits);
        }
        // emitTaskOutput.get(1-emmitTaskRunnableNum).get(id)
    }

    static void merge() {
        ArrayList<WCResult> topTen = new ArrayList<WCResult>(16);
        int index[] = new int[rankTaskRunnableNum];

        for (int i = 0; i < 10; i++) {
            WCResult max = null;
            String top = null;
            int maxIndex = -1;
            for (int j = 0; j < rankTaskRunnableNum; j++) {
                if (index[j] > 10) continue;
                WCResult r = rankTaskOutput[j][index[j]];
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

    private static boolean isSplitter(byte c) {
        for (byte p : punctuation) {
            if (p == c) return true;
        }
        return false;
    }

    static class EmmitTaskRunnable implements Runnable {
        int id;
        int offset;
        ArrayList<HashMap<String, MutableInt>> output;

        EmmitTaskRunnable(int id) {
            this.id = id;
            this.output = emitTaskOutput.get(id);
            this.offset = id * splitSize;
        }

        public void run() {
            try {
                int padding = 64;
                int len = splitSize;


                if (id == emmitTaskRunnableNum - 1) {
                    len = fileLength % splitSize + splitSize;
                } else {
                    len += padding;
                }

                if (id != 0) {
                    offset -= padding;
                    len += padding;
                }
                MappedByteBuffer input = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, len);

                // 切分头部处理
                boolean giveupFirst = true;
                if (id != 0) { // 第一块不用预读取
                    byte[] paddingBuffer = new byte[padding];
                    input.get(paddingBuffer, 0, padding);
                    len -= padding;
                    if (isSplitter(paddingBuffer[padding - 1])) giveupFirst = false;
                }

                boolean isLastSplitter = true;
                byte[] buffer = new byte[1024];
                int wordLen = 0;
                while (input.hasRemaining()) {
                    if (len - padding <= 0) break;
                    byte c = input.get();
                    len--;
                    if (!isSplitter(c)) {
                        if (c >= 'A' && c <= 'Z') c += 32;
                        buffer[wordLen++] = c;
                        isLastSplitter = false;
                    } else {
                        // 只有前一个字符不是分隔符，后一个字符是分隔符才是一个word分界
                        if (isLastSplitter) continue;

                        // 不是所有满足word分界规则的就一定是完整的单词， 整块的第一个单词可能不是完整的单词，只有整块之前的一个字符为splitter才是
                        if (giveupFirst) {
                            giveupFirst = false;
                            continue;
                        }

                        // split word
                        String word = new String(buffer, 0, wordLen);
                        // word cnt
                        int index = word.hashCode() % output.size();
                        HashMap<String, MutableInt> rankSplit = output.get(index > 0 ? index : 0 - index);
                        MutableInt cnt = rankSplit.get(word);
                        if (cnt == null) rankSplit.put(word, new MutableInt());
                        else cnt.inc();

                        // reset
                        wordLen = 0;
                    }
                }

                // 切分尾部处理
                if (id != emmitTaskRunnableNum - 1) {
                    byte[] paddingBuffer = new byte[padding];
                    input.get(paddingBuffer, 0, padding);
                    len -= padding;

                    if (!isSplitter(paddingBuffer[0])) {
                        for (int i = 0; i < padding; i++) {
                            byte c = paddingBuffer[i];
                            if (isSplitter(c)) break;
                            buffer[wordLen++] = c;
                        }

                        // split word
                        String word = new String(buffer, 0, wordLen);
                        // word cnt
                        int index = word.hashCode() % output.size();
                        HashMap<String, MutableInt> cntSplit = output.get(index > 0 ? index : 0 - index);
                        MutableInt cnt = cntSplit.get(word);
                        if (cnt == null) cntSplit.put(word, new MutableInt());
                        else cnt.inc();
                    }
                }

                emmitTaskBarrier.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class RankTaskRunnable implements Runnable {
        final int id;
        final WCResult[] rank;

        RankTaskRunnable(int id) {
            this.id = id;
            rank = new WCResult[10000];
            rankTaskOutput[id] = rank;
        }

        public void run() {
            int len = 0;
            int top = 10;
            for (int m = 0; m < emmitTaskRunnableNum; m++) {
                HashMap<String, MutableInt> cntSplit = emitTaskOutput.get(m).get(id);
                for (Map.Entry<String, MutableInt> entry : cntSplit.entrySet()) {
                    int cnt = entry.getValue().value;
                    String word = entry.getKey();

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
            }
            rankTaskBarrier.countDown();

        }
    }
    static boolean isStopWords(String word) {
        if (word.length() > 6) {// stopword 特征
            return false;
        }
        return stopWords.contains(word);
    }
    static class MutableInt {
        int value = 1;

        void inc() {
            value++;
        }
    }

    static class WCResult {
        final String word;
        final int cnt;

        WCResult(String word, int cnt) {
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
}
