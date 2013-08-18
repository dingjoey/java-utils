package com.taobao.joey.wordcnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-8-18
 * Time: ÏÂÎç1:54
 */
public class WC1 {
    //
    static int PAGE_SIZE = 65535;
    static byte[] chunk;
    //
    static byte[] punctuation
            = {'.', ',', ';', '-', '~', '?', '!', '\'', '\"', '\r', '\n', '\t', ' '};
    static Map<String, Integer> wordCnt = new HashMap<String, Integer>(40000, 0.99f);
    //
    static List<String> wordRank = new ArrayList<String>(10);
    static List<Integer> cntRank = new ArrayList<Integer>(10);
    static HashSet<String> stopWords
            = new HashSet<String>(Arrays.asList("the", "and", "i", "to", "of", "a", "in", "was", "that", "had", "he", "you", "his", "my", "it", "as", "with", "her", "for", "on"));

    public static void main(String[] args) throws IOException, URISyntaxException {
        long startReadFile = System.currentTimeMillis();
        mmapRead();
        long endReadFile = System.currentTimeMillis();

        long startWordCnt = System.currentTimeMillis();
        wordCnt(0, chunk.length);
        long endWordCnt = System.currentTimeMillis();

        long startTopTen = System.currentTimeMillis();
        topTen();
        long endTopTen = System.currentTimeMillis();

        System.out.println("read file consumes: " + (endReadFile - startReadFile) + "ms");
        System.out.println("word cnt consumes: " + (endWordCnt - startWordCnt) + "ms");
        System.out.println("rank consumes: " + (endTopTen - startTopTen) + "ms");
    }

    static void mmapRead() throws IOException, URISyntaxException {
        File file = new File(ReadFileTest.class.getResource("/document.txt").toURI());

        MappedByteBuffer buffer = new RandomAccessFile(file, "rw").getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, file.length());

        long fileLength = file.length();
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
    static void wordCnt(int startIndex, int endIndex) {
        String all = new String(chunk, startIndex, endIndex - startIndex);
        String[] words = all.split("\\W+");
        for (String word : words) {
            String lower = word.toLowerCase();
            Integer cnt = wordCnt.get(lower);
            if (cnt == null) {
                wordCnt.put(lower, 1);
            } else {
                wordCnt.put(lower, ++cnt);
            }
        }
    }

    /**
     *
     */
    static void topTen() {
        for (Map.Entry<String, Integer> entry : wordCnt.entrySet()) {
            String word = entry.getKey();
            int cnt = entry.getValue();

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

}
