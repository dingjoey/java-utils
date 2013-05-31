package com.taobao.joey.btree;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-24
 * Time: обнГ1:40
 */
public interface IBTree<KEY_TYPE, DATA_TYPE> {

    public boolean get(KEY_TYPE searchKey, DATA_TYPE foundData);

    public boolean insertOrUpdate(KEY_TYPE insertKey, DATA_TYPE newData);

    public boolean delete(KEY_TYPE deleteKey);

}
