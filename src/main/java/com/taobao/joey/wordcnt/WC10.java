package com.taobao.joey.wordcnt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
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
    //
    static FileChannel fileChannel;
    static File file;
    static int splitSize;
    static int fileLength;
    static MappedByteBuffer input;
    //
    static int emmitTaskRunnableNum = 4;
    static ArrayList<HashMap<String, MutableInt>> emitTaskOutput = new ArrayList<HashMap<String, MutableInt>>(emmitTaskRunnableNum);
    static CountDownLatch emmitTaskBarrier = new CountDownLatch(emmitTaskRunnableNum);

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        // init
        init(4);

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

        //

    }

    public static void init(int en) throws URISyntaxException, IOException {
        // init emmit task thread num
        emmitTaskRunnableNum = en;
        // init emmit task input
        file = new File(WC10.class.getResource("/document.txt").toURI());
        fileChannel = new RandomAccessFile(file, "rw").getChannel();
        fileLength = (int) file.length();
        splitSize = fileLength / emmitTaskRunnableNum;
        // init emmit task output
        for (int i = 0; i < emmitTaskRunnableNum; i++) {
            emitTaskOutput.add(new HashMap<String, MutableInt>(50000, 09f));
        }
        // init emmit task barrier
        emmitTaskBarrier = new CountDownLatch(emmitTaskRunnableNum);
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
        HashMap<String, MutableInt> output;

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
                        MutableInt cnt = output.get(word);
                        if (cnt == null) output.put(word, new MutableInt());
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
                        MutableInt cnt = output.get(word);
                        if (cnt == null) output.put(word, new MutableInt());
                        else cnt.inc();
                    }
                }

                emmitTaskBarrier.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class MutableInt {
        int value = 1;

        void inc() {
            value++;
        }
    }
}
