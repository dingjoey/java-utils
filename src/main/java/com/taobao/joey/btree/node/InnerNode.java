package com.taobao.joey.btree.node;

import com.taobao.joey.btree.BTreeImpl;
import com.taobao.joey.btree.reference.ChildReference;
import com.taobao.joey.btree.util.TreeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-24
 * Time: 下午1:36
 */
public class InnerNode<KEY_TYPE> extends Node<KEY_TYPE> {
    /**
     * RootNode/InnerNode children 用于保存key range ptr to InnderNode&LeafNode
     */
    protected List<Node> childrenRefs = new ArrayList<Node>();

    public InnerNode(Node parent, BTreeImpl tree) {
        super(parent, tree);
    }

    public List<Node> getChildrenRefs() {
        return childrenRefs;
    }

    /**
     * 点是否超出饱满度了? key > order || ptr > order+1
     *
     * @return
     */
    @Override
    public boolean isOverflow() {
        final int MAX_CHILDREN_CNT = tree.getOrder() + 1;
        final int MAX_KEYENTRY_CNT = tree.getOrder();
        return childrenRefs.size() > MAX_CHILDREN_CNT || keys.size() > MAX_KEYENTRY_CNT;
    }

    /**
     * 节点是否低于最小饱满度? key < order/2(向上取整) || ptr < order/2(向上取整)+1
     *
     * @return
     */
    @Override
    public boolean isUnderflow() {
        final int MIN_KEYENTRY_CNT = tree.getOrder() / 2;
        final int MIN_CHILDREN_CNT = tree.getOrder() / 2 + 1;
        return childrenRefs.size() < MIN_CHILDREN_CNT || keys.size() < MIN_KEYENTRY_CNT;
    }

    /**
     * dump BTree node into String
     *
     * @return
     */
    @Override
    protected String dumpString(int nSpaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("<innerNode>");

        for (int i = 0; i < keys.size(); i++) {
            sb.append('\n');
            sb.append(TreeUtils.indent(nSpaces + 1));
            sb.append("<key>").append(keys.get(i).toString()).append("</key>");

            sb.append('\n');
            sb.append(TreeUtils.indent(nSpaces + 1));
            sb.append(childrenRefs.get(i).dumpString(nSpaces + 1));
        }

        sb.append('\n');
        sb.append("</innerNode>");

        return sb.toString();
    }

    /**
     * @param splitLeft
     * @param splitRight
     * @param insertIndex
     */
    public void insert(KEY_TYPE splitKey, ChildReference<KEY_TYPE> splitLeft, ChildReference<KEY_TYPE> splitRight, int insertIndex) {
        if (keys.size() > 0) {
            keys.remove(insertIndex);
        }
        if (childrenRefs.size() > 0) {
            childrenRefs.remove(insertIndex);
        }

        keys.add(insertIndex, splitKey);
        // 注意顺序 先插入splitRight，再插入splitLeft，因为splitRight.key > splitLeft.key
        childrenRefs.add(insertIndex, splitRight.target);
        childrenRefs.add(insertIndex, splitLeft.target);
    }
}
