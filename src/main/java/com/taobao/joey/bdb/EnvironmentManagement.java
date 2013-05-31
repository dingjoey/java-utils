package com.taobao.joey.bdb;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-23
 * Time: 上午11:46
 * http://docs.oracle.com/cd/E17277_02/html/GettingStartedGuide/dbenvUsageExample.html
 */
public class EnvironmentManagement {
    private static Logger LOG = LoggerFactory.getLogger(EnvironmentManagement.class);
    private Environment env;

    public static void main(String[] args) {
        EnvironmentManagement em = new EnvironmentManagement();
        try {
            // Step 1：确保Environment Dir存在
            File envDir = new File("./dbEnv");
            if (!envDir.isDirectory()) {
                if (!envDir.mkdirs()) {
                    LOG.warn("error creating environment dir");
                    return;
                }
            }
            // Step 2: 创建Environment
            em.setup(envDir, false);

        } catch (DatabaseException e) {
            LOG.warn("error creating environment", e);
        } finally {
            // Step 3: close environment
            em.close();
        }
    }

    public Environment getEnv() {
        return env;
    }

    public void setup(File envHome, boolean readOnly) throws DatabaseException {
        // Step 1:创建Environment的配置
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setReadOnly(readOnly);
        envConfig.setAllowCreate(!readOnly); // If true, creates the database environment if it doesn't already exist. 和Env文件无关
        // TODO other evnConfig settings

        // Step 2:打开(maybe 创建)Environment，返回句柄
        env = new Environment(envHome, envConfig);
    }

    public void close() {
        if (env != null) {
            try {
                env.close();
            } catch (DatabaseException e) {
                LOG.warn("Error closing environment", e);
            }
        }
    }
}
