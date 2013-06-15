package com.taobao.joey.benchmark;

import org.junit.Test;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-15
 * Time: ÉÏÎç10:16
 */
public class BenchmarkTest {
    @Test
    public void test() {
        Benchmark benchmark = new Benchmark();
        Benchmark.BenchMethodRunnable method = new Benchmark.BenchMethodRunnable() {
            public void run(Stats stats) {
                stats.bytes++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                stats.finishSingleOp();
            }
        };
        benchmark.run(5, 5000, "test-bench", method);
    }
}
