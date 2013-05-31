package com.taobao.joey.btree.node;

import com.taobao.joey.btree.BTreeImpl;
import com.taobao.joey.btree.reference.DataReference;
import com.taobao.joey.btree.util.TreeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-24
 * Time: 下午1:35
 */
public class LeafNode<KEY_TYPE, DATA_TYPE> extends Node<KEY_TYPE> {
    /**
     * leafNode keyEntries.size == dataRefs.size; dataRefs保存对应数据Ref
     */
    private List<DATA_TYPE> dataRefs = new ArrayList<DATA_TYPE>();
    /**
     * LeafNode preivious/next != null; otherwise, like RootNode/InnerNode preivious/next == null;
     */
    private LeafNode previous;
    private LeafNode next;

    public LeafNode(Node parent, BTreeImpl tree) {
        super(parent, tree);
    }

    public List<DATA_TYPE> getDataRefs() {
        return dataRefs;
    }

    public LeafNode getPrevious() {
        return previous;
    }

    public void setPrevious(LeafNode previous) {
        this.previous = previous;
    }

    public LeafNode getNext() {
        return next;
    }

    public void setNext(LeafNode next) {
        this.next = next;
    }

    /**
     * 点是否超出饱满度了? key > order || ptr > order+1
     *
     * @return
     */
    @Override
    public boolean isOverflow() {
        final int MAX_CHILDREN_CNT = tree.getOrder();
        final int MAX_KEYENTRY_CNT = tree.getOrder();
        return dataRefs.size() > MAX_CHILDREN_CNT || keys.size() > MAX_KEYENTRY_CNT;
    }

    /**
     * 节点是否低于最小饱满度? key < order/2(向上取整) || ptr < order/2(向上取整)+1
     *
     * @return
     */
    @Override
    public boolean isUnderflow() {
        final int MIN_KEYENTRY_CNT = tree.getOrder() / 2;
        final int MIN_CHILDREN_CNT = tree.getOrder() / 2;
        return dataRefs.size() < MIN_CHILDREN_CNT || keys.size() < MIN_KEYENTRY_CNT;
    }

    /**
     * dump BTree node into String
     *
     * @return
     */
    @Override
    protected String dumpString(int nSpaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("<leafNode>");

        for (int i = 0; i < keys.size(); i++) {
            sb.append('\n');
            sb.append(TreeUtils.indent(nSpaces + 1));
            sb.append("<key>").append(keys.get(i).toString()).append("</key>");

            sb.append('\n');
            sb.append(TreeUtils.indent(nSpaces + 1));
            sb.append("<dataRef>").append(dataRefs.get(i).toString()).append("</dataRef>");
        }

        sb.append('\n');
        sb.append("</leafNode>");

        return sb.toString();
    }

    /**
     *
     * @param dataRef
     * @param insertIndex
     */
    public void insert(DataReference<KEY_TYPE,DATA_TYPE> dataRef, int insertIndex) {
        keys.add(insertIndex, dataRef.key);
        dataRefs.add(insertIndex, dataRef.dataRef);
    }
}
