package com.taobao.joey.bdb;

import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-27
 * Time: 下午2:44
 */
public class OpsUtils {

    private static Logger LOG = LoggerFactory.getLogger(OpsUtils.class);

    //public static <K, T> void

    public static <K, T> void putPrimitive(Database db, Class<K> keyClazz, K key, Class<T> dataClazz, T data) throws DatabaseException {
        DatabaseEntry keyEntry = DatabaseEntryUtils.primitive2Entry(keyClazz, key);
        DatabaseEntry dataEntry = DatabaseEntryUtils.primitive2Entry(dataClazz, data);
        db.put(null, keyEntry, dataEntry);
    }

    public static <K, T> T getPrimitive(Database db, Class<K> keyClazz, K key, Class<T> dataClazz) throws DatabaseException {
        DatabaseEntry keyEntry = DatabaseEntryUtils.primitive2Entry(keyClazz, key);
        DatabaseEntry dataEntry = new DatabaseEntry();

        if (db.get(null, keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return DatabaseEntryUtils.Entry2Primitive(dataClazz, dataEntry);
        } else {
            return null;
        }
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

            OpsUtils.putPrimitive(dm.getDb(), String.class, "testkey1", String.class, "testvalue");
            String value = OpsUtils.getPrimitive(dm.getDb(), String.class, "testkey1", String.class);
            LOG.debug(value);
            value = OpsUtils.getPrimitive(dm.getDb(), String.class, "testkey1", String.class);
            LOG.debug(value);

        } catch (DatabaseException e) {
            LOG.warn("Error creating dbbase!", e);
        } finally {
            dm.close();
            em.close();
        }
    }


}
