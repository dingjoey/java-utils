package com.taobao.joey.benchmark;

import org.junit.Test;

import java.util.Random;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-14
 * Time: обнГ3:59
 */
public class HistogramTest {
    @Test
    public void test() {
        Histogram histogram = new Histogram();
        histogram.clear();

        Random random = new Random(System.currentTimeMillis());
        for (int i = 1; i < 8888; i++) {
            double value = random.nextInt(10000000);
            histogram.sample(value);
        }

        System.out.println(histogram.toString());
    }
}
