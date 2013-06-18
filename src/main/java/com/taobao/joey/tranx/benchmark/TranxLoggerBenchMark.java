package com.taobao.joey.tranx.benchmark;

import com.taobao.joey.benchmark.Benchmark;
import com.taobao.joey.benchmark.Stats;
import com.taobao.joey.tranx.LogEntry;
import com.taobao.joey.tranx.Loggable;
import com.taobao.joey.tranx.TranxLogger;

import java.util.Arrays;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-15
 * Time: ÉÏÎç10:24
 */
public class TranxLoggerBenchMark {
    private static TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");

    public static void main(String[] args) {
        logger.openLogging();

        Benchmark benchmark = new Benchmark();
        Benchmark.BenchMethodRunnable method = new Benchmark.BenchMethodRunnable() {
            public void run(Stats stats) {

                long id = TranxLogger.nextTranxId();
                LogEntry start = LogEntry.StartTranxLogEntry.newEntry(id);
                LogEntry commit = LogEntry.CommitTranxLogEntry.newEntry(id);

                logger.logTranx(Arrays.<Loggable>asList(start, commit));
                logger.sync();

                stats.finishSingleOp();

            }
        };
        benchmark.run(1, 5000, "logTranx", method);

        logger.closeLogging();
    }
}
