package com.taobao.joey.tranx;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-4
 * Time: обнГ2:33
 */
public interface Loggable {
    /**
     * write loggable to buffer
     *
     * @param buffer
     */
    public boolean writeToLog(DataOutputBuffer buffer);

    /**
     * read loggable from buffer
     *
     * @param buffer
     */
    public boolean readFromLog(DataInputBuffer buffer);
}
