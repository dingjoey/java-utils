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
 * Time: 下午3:59
 */
public class WC5 {
    //
    static int PAGE_SIZE = 65535;
    static byte[] chunk;
    //
    static long fileLength;
    //
    static int splitThreadNum = 8;
    static int countThreadNum = 8;
    static int mergeThreadNum = 1;
    static CountDownLatch splitTaskBarrier = new CountDownLatch(splitThreadNum);
    static CountDownLatch countTaskBarrier = new CountDownLatch(countThreadNum);
    // splitTaskId -> countTaskInput list; c
    static List<List<List<String>>> splitTaskOutput = new ArrayList<List<List<String>>>(splitThreadNum);
    static List<List<WordCntResult>> countTaskOutput = new ArrayList<List<WordCntResult>>(countThreadNum);

    static {
        init(4, 4, 1);
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        if (args.length == 2) {
            init(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 1);
        }

        long startReadFile = System.currentTimeMillis();
        mmapRead();
        long endReadFile = System.currentTimeMillis();

        long startSplitTask = System.currentTimeMillis();
        Thread[] threads0 = new Thread[splitThreadNum];
        for (int i = 0; i < splitThreadNum; i++) {
            threads0[i] = new Thread(new SplitTaskRunnable(i));
            threads0[i].start();
        }
        splitTaskBarrier.await();
        long endSplitTask = System.currentTimeMillis();

        long startCntTask = System.currentTimeMillis();
        Thread[] threads1 = new Thread[countThreadNum];
        for (int i = 0; i < countThreadNum; i++) {
            threads1[i] = new Thread(new CountTaskRunnable(i));
            threads1[i].start();
        }
        countTaskBarrier.await();
        long endCntTask = System.currentTimeMillis();

        System.out.println("finish count");
        long startMergeTask = System.currentTimeMillis();
        new MergeTaskRunnable(0).run();
        long endMergeTask = System.currentTimeMillis();

        System.out.println("read file consumes: " + (endReadFile - startReadFile) + "ms");
        System.out.println("split task consumes: " + (endSplitTask - startSplitTask) + "ms");
        System.out.println("count task consumes: " + (endCntTask - startCntTask) + "ms");
        System.out.println("merge task consumes: " + (endMergeTask - startMergeTask) + "ms");

        System.out.println("total consumes: " + (endMergeTask - startReadFile) + "ms");


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

    static void init(int sn, int cn, int mn) {
        splitThreadNum = sn;
        countThreadNum = cn;
        mergeThreadNum = mn;
        //
        splitTaskBarrier = new CountDownLatch(splitThreadNum);
        countTaskBarrier = new CountDownLatch(countThreadNum);
        //
        splitTaskOutput = new ArrayList<List<List<String>>>(splitThreadNum);
        for (int i = 0; i < splitThreadNum; i++) {
            //split task的输出 1对1对应为count Task的输入
            List<List<String>> output = new ArrayList<List<String>>(countThreadNum);
            for (int j = 0; j < countThreadNum; j++) {
                output.add(new ArrayList<String>(100000));
            }
            splitTaskOutput.add(output);
        }
        countTaskOutput = new ArrayList<List<WordCntResult>>(countThreadNum);
        for (int i = 0; i < countThreadNum; i++) {
            countTaskOutput.add(new ArrayList<WordCntResult>(100000));
        }
    }

    static class SplitTaskRunnable implements Runnable {
        static byte[] punctuation
                = {'.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
        final int id;
        final List<List<String>> output;

        SplitTaskRunnable(int id) {
            this.id = id;
            output = splitTaskOutput.get(id);
        }

        public void run() {
            int splitLength = (int) (fileLength / splitThreadNum);

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
                        byte[] wordByte = Arrays.copyOfRange(chunk, wordStartIndex, wordEndIndex + 1);
                        String word = new String(wordByte).toLowerCase();
                        int index = word.hashCode() % countThreadNum;
                        output.get(index > 0 ? index : 0 - index).add(word);
                        wordStartIndex = -1;
                    }
                }
            }

            splitTaskBarrier.countDown();
        }

        boolean isSplitter(byte c) {
            for (byte p : punctuation) {
                if (p == c) return true;
            }
            return false;
        }
    }

    static class CountTaskRunnable implements Runnable {
        final int id;
        final List<String> input = new ArrayList<String>();
        final List<WordCntResult> output;

        CountTaskRunnable(int id) {
            this.id = id;
            long start = System.currentTimeMillis();
            for (List<List<String>> output : splitTaskOutput) {
                for (int i = 0; i < output.size(); i++) {
                    if (i % countThreadNum == id) {
                        input.addAll(output.get(i));
                    }
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("count input create consumes: " + (end - start));
            output = countTaskOutput.get(id);
        }

        public void run() {
            long start = System.currentTimeMillis();
            Collections.sort(input);
            long end = System.currentTimeMillis();
            System.out.println("count input create consumes: " + (end - start));

            String last = input.get(0);
            int cnt = 0;
            for (String word : input) {
                if (word.equals(last)) {
                    cnt++;
                } else {
                    // insert sort && get top 10
                    int i = 0;
                    for (; i < output.size(); i++) {
                        if (cnt > output.get(i).cnt) break;
                    }

                    if (i < output.size()) {
                        output.add(i, new WordCntResult(last, cnt));
                    }

                    if (i == output.size() && output.size() < 10) {
                        output.add(new WordCntResult(last, cnt));
                    }

                    cnt = 0;
                    last = word;
                }
            }
            countTaskBarrier.countDown();
        }
    }

    static class MergeTaskRunnable implements Runnable {
        static HashSet<String> stopWords
                = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));
        final int id;
        final List<List<WordCntResult>> input;

        MergeTaskRunnable(int id) {
            this.id = id;
            input = countTaskOutput;
        }

        public void run() {
            int index[] = new int[input.size()];
            for (int i = 0; i < index.length; i++) {
                index[i] = 0;
            }
            ArrayList<WordCntResult> topTen = new ArrayList<WordCntResult>();

            for (int i = 0; i < 10 + stopWords.size(); i++) {
                WordCntResult max = null;
                int maxIndex = -1;
                for (int j = 0; j < index.length; j++) {
                    WordCntResult cur = input.get(j).get(index[j]);
                    if (max == null || cur.cnt > max.cnt) {
                        max = cur;
                        maxIndex = j;
                    }
                }
                index[maxIndex]++;
                topTen.add(max);
            }

            int cnt = 10;
            for (WordCntResult result : topTen) {
                if (!stopWords.contains(result.word)) {
                    System.out.println(result);
                    cnt--;
                    if (cnt == 0) break;
                }
            }
        }
    }

    static class WordCntResult {
        final String word;
        final int cnt;

        WordCntResult(String word, int cnt) {
            this.word = word;
            this.cnt = cnt;
        }

        @Override
        public String toString() {
            return "WordCntResult{" +
                    "word='" + word + '\'' +
                    ", cnt=" + cnt +
                    '}';
        }
    }
}
