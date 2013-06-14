package com.taobao.joey.tranx;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-13
 * Time: ÏÂÎç1:18
 */
public class TranxLoggerTest {

    @Test
    public void test_initTranxLogger() {
        TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");
        Assert.assertNotNull("create tranx logger success!", logger);
        logger = TranxLogger.getTranxLogger(null, "test-tranx");
        Assert.assertNotNull("create tranx logger success!", logger);
        TranxLogger logger1 = TranxLogger.getTranxLogger(null, "test-tranx");
        Assert.assertNotNull("create tranx logger success!", logger1);
    }

    @Test
    public void test_openLogging() {
        TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");
        Assert.assertNotNull("create tranx logger success!", logger);
        boolean succ = logger.openLogging();
        Assert.assertTrue("successfully open logging!", succ);

        succ = logger.closeLogging();
        Assert.assertTrue("successfully close logging!", succ);
    }

    @Test
    public void test_Logging() throws IOException {
        LogEntry start = LogEntry.StartTranxLogEntry.newEntry(1l);
        LogEntry commit = LogEntry.CommitTranxLogEntry.newEntry(1l);

        TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");
        Assert.assertNotNull("create tranx logger success!", logger);
        boolean succ = logger.openLogging();
        Assert.assertTrue("successfully open logging!", succ);

        logger.logTranx(Arrays.<Loggable>asList(start, commit));
        logger.sync();

        succ = logger.openRecovering(20);
        succ = logger.recover();
        succ = logger.closeRecovering();

        succ = logger.closeLogging();
        Assert.assertTrue("successfully close logging!", succ);

    }

    @Test
    public void test_openRecovering() throws IOException {
        //List<Loggable> loggables = new ArrayList<Loggable>();
        TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");
        boolean succ = logger.openRecovering(1);
        Assert.assertTrue("successfully open recovering!", succ);
        succ = logger.closeRecovering();
        Assert.assertTrue("successfully close recovering!", succ);
    }

    @Test
    public void test_reading() throws IOException {
        TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");
        boolean succ = logger.openRecovering(20);
        Assert.assertTrue("successfully open recovering!", succ);

        succ = logger.recover();
        Assert.assertTrue("", succ);

        succ = logger.closeRecovering();
        Assert.assertTrue("successfully close recovering!", succ);
    }

    @Test
    public void test_log2newFile() {
        TranxLogger logger = TranxLogger.getTranxLogger("D://", "test-tranx");
        Assert.assertNotNull("create tranx logger success!", logger);
        boolean succ = logger.openLogging();
        Assert.assertTrue("successfully open logging!", succ);

        for (int i = 0; i < 10000; i++) {
            long id = TranxLogger.nextTranxId();
            LogEntry start = LogEntry.StartTranxLogEntry.newEntry(id);
            LogEntry commit = LogEntry.CommitTranxLogEntry.newEntry(id);

            logger.logTranx(Arrays.<Loggable>asList(start, commit));
            logger.sync();
        }

    }

}
