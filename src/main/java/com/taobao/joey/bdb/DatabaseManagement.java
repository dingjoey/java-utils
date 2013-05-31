package com.taobao.joey.bdb;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-27
 * Time: 下午1:24
 */
public class DatabaseManagement {
    private static Logger LOG = LoggerFactory.getLogger(DatabaseManagement.class);
    private EnvironmentManagement em;
    private Database db;

    public DatabaseManagement(EnvironmentManagement em) {
        this.em = em;
    }

    public static void main(String[] args) {
        EnvironmentManagement em = new EnvironmentManagement();
        DatabaseManagement dm = new DatabaseManagement(em);
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

            // Step 3: 创建DB
            dm.setup(false, "testDB");
        } catch (DatabaseException e) {
            LOG.warn("Error creating dbbase!", e);
        } finally {
            dm.close();
            em.close();
        }
    }

    public Database getDb() {
        return db;
    }

    public void setup(boolean readOnly, String dbName) throws DatabaseException {
        // Step 1:
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setReadOnly(readOnly);
        dbConfig.setAllowCreate(!readOnly);
        // TODO other dbConfig settings
        //dbConfig.setTemporary(false);
        //dbConfig.setTransactional(false);

        // Step 2:
        db = em.getEnv().openDatabase(null, dbName, dbConfig);
    }

    public void close() {
        if (db != null) {
            try {
                db.close();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }
}
