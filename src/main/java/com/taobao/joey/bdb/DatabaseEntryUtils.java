package com.taobao.joey.bdb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-27
 * Time: 下午1:39
 */
public class DatabaseEntryUtils {
    private static Logger LOG = LoggerFactory.getLogger(DatabaseEntryUtils.class);

    public static DatabaseEntry Str2Entry(String str, String enc) throws UnsupportedEncodingException {
        return new DatabaseEntry(str.getBytes(enc));
    }

    public static String Entry2Str(DatabaseEntry entry, String enc) throws UnsupportedEncodingException {
        return new String(entry.getData(), enc);
    }

    public static <T> DatabaseEntry primitive2Entry(Class<T> clazz, T data) {
        DatabaseEntry entry = new DatabaseEntry();
        EntryBinding<T> binding = TupleBinding.getPrimitiveBinding(clazz);
        binding.objectToEntry(data, entry);
        return entry;
    }

    public static <T> T Entry2Primitive(Class<T> clazz, DatabaseEntry entry) {
        EntryBinding<T> binding = TupleBinding.getPrimitiveBinding(clazz);
        return binding.entryToObject(entry);
    }

    /**
     * 通过binding api，利用序列化方案定义数据模型
     *
     * @param classDb
     * @param clazz
     * @param data
     * @param <T>
     * @return
     * @throws com.sleepycat.je.DatabaseException
     */
    public static <T> DatabaseEntry serializable2Entry(Database classDb, Class<T> clazz, T data) throws DatabaseException {
        //Instantiate a class catalog
        StoredClassCatalog classCatalog = new StoredClassCatalog(classDb);
        // Create the binding
        EntryBinding<T> dataBinding = new SerialBinding(classCatalog, clazz);

        DatabaseEntry entry = new DatabaseEntry();
        dataBinding.objectToEntry(data, entry);
        return entry;
    }

    public static <T> T entry2Serializable(Database classDb, Class<T> clazz, DatabaseEntry entry) throws DatabaseException {
        //Instantiate a class catalog
        StoredClassCatalog classCatalog = new StoredClassCatalog(classDb);
        // Create the binding
        EntryBinding<T> dataBinding = new SerialBinding(classCatalog, clazz);

        return dataBinding.entryToObject(entry);
    }


}
