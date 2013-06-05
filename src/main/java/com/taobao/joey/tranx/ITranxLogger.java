package com.taobao.joey.tranx;

import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-6-5
 * Time: ионГ10:01
 */
public interface ITranxLogger {

    /**
     * record single operation log
     *
     * @param loggable
     */
    public void log(Loggable loggable);

    /**
     * record tranxaction operations' log
     *
     * @param tranx
     */
    public void logTranx(List<Loggable> tranx);
}
