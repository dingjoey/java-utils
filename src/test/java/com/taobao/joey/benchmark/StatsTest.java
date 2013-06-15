package com.taobao.joey.benchmark;

import org.junit.Test;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-14
 * Time: обнГ4:20
 */
public class StatsTest {
    @Test
    public void test() {
        Stats stats = new Stats();
        stats.opName = "testOps";

        stats.start();
        for (int i = 1; i < 288888888; i++) {
            stats.finishSingleOp();
        }
        stats.stop();

        stats.report();
    }
}
