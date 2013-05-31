package com.taobao.joey.btree.node;

import com.taobao.joey.btree.BTreeImpl;
import com.taobao.joey.btree.IBTree;

import java.util.ArrayList;
import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-24
 * Time: ����1:18
 */
public abstract class Node<KEY_TYPE> {
    /**
     * ��������
     */
    protected List<KEY_TYPE> keys = new ArrayList<KEY_TYPE>();
    /**
     * RootNode parent == null; otherwise, like LeafNode/InnerNode parent != null;
     */
    protected Node parent;
    /**
     * a reference to the IBTree,which the current node belongs to
     */
    protected BTreeImpl tree;

    protected Node(Node parent, BTreeImpl tree) {
        this.parent = parent;
        this.tree = tree;
    }

    /**
     * ���Ƿ񳬳���������? key > order || ptr > order+1
     *
     * @return
     */
    public abstract boolean isOverflow();

    /**
     * �ڵ��Ƿ������С������? key < order/2(����ȡ��) || ptr < order/2(����ȡ��)+1
     *
     * @return
     */
    public abstract boolean isUnderflow();

    /**
     * dump BTree node into String
     *
     * @return
     */
    protected abstract String dumpString(int nSpaces);

    /**
     * for test
     *
     * @return
     */
    public void dump() {
        System.out.println(dumpString(0));
    }

    public IBTree getTree() {
        return tree;
    }

    public void setTree(BTreeImpl tree) {
        this.tree = tree;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<KEY_TYPE> getKeys() {
        return keys;
    }

    public void setKeys(List<KEY_TYPE> keys) {
        this.keys = keys;
    }
}
