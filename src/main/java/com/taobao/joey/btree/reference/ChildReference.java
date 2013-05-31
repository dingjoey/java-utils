package com.taobao.joey.btree.reference;

import com.taobao.joey.btree.node.Node;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-26
 * Time: обнГ3:46
 */
public class ChildReference<KEY_TYPE> implements NodeReference {
    public KEY_TYPE key;
    public Node target;

    public ChildReference(KEY_TYPE key, Node target) {
        this.key = key;
        this.target = target;
    }
}
