package com.taobao.joey.btree;

import com.taobao.joey.btree.node.InnerNode;
import com.taobao.joey.btree.node.LeafNode;
import com.taobao.joey.btree.node.Node;
import com.taobao.joey.btree.node.RootNode;
import com.taobao.joey.btree.reference.ChildReference;
import com.taobao.joey.btree.reference.DataReference;

import java.util.Comparator;
import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-24
 * Time: 下午1:37
 * <p/>
 * 无重复+稠密
 */
public final class BTreeImpl<KEY_TYPE, DATA_TYPE> implements IBTree<KEY_TYPE, DATA_TYPE> {
    Comparator<KEY_TYPE> comparator;
    private int order;
    private RootNode<KEY_TYPE, DATA_TYPE> root;

    public BTreeImpl(Comparator<KEY_TYPE> comparator, int order) {
        this.comparator = comparator;
        this.order = order;
        this.root = new RootNode<KEY_TYPE, DATA_TYPE>(null, this);

        if (order < 1) {

        }

        if (comparator == null) {

        }

    }

    public static void main(String[] args) {
        BTreeImpl<Integer, String> btree = new BTreeImpl<Integer, String>(new IntegerComparator(), 3);
        btree.insertOrUpdate(2, "haha");
        btree.insertOrUpdate(1, "xixi");
        btree.insertOrUpdate(0, "hoho");
        btree.insertOrUpdate(6, "important");
        btree.dump();
    }

    public int getOrder() {
        return order;
    }

    public boolean get(KEY_TYPE searchKey, DATA_TYPE foundData) {
        // 根据key值从root开始一直找到对应leaf节点
        Node node = innerGet(root, searchKey);

        //遍历叶子节点中的key，比较searchKey，相等则返回对应dataRef
        List<KEY_TYPE> keys = node.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            if (comparator.compare(keys.get(i), searchKey) == 0) {
                //data.dataRef = new String("hit"); //TODO set data
                return true;
            }
        }

        // TODO
        return false;
    }

    private int computeInsertIndex(Node node, KEY_TYPE insertKey) {
        int insertIndex = node.getKeys().size();

        if (node instanceof LeafNode
                || (node instanceof RootNode && ((RootNode) node).getChildrenRefs().size() == 0)) {
            for (int i = 0; i < node.getKeys().size(); i++) {
                KEY_TYPE curr = (KEY_TYPE) node.getKeys().get(i);
                if (comparator.compare(curr, insertKey) > 0) {  // 每个节点都是增序列排列的
                    insertIndex = i;
                    break;
                }
            }
        } else if (node instanceof InnerNode) {
            for (int i = 1; i < node.getKeys().size(); i++) {
                KEY_TYPE curr = (KEY_TYPE) node.getKeys().get(i);
                if (comparator.compare(curr, insertKey) > 0) {  // 每个节点都是增序列排列的
                    insertIndex = i;
                    break;
                }
            }
        }
        return insertIndex;
    }

    public boolean insertOrUpdate(KEY_TYPE insertKey, DATA_TYPE newData) {

        Node node = innerGet(root, insertKey);

        // update

        List<KEY_TYPE> keys = node.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            if (comparator.compare(keys.get(i), insertKey) == 0) {
                //TODO update
                return true;
            }
        }

        // insert
        DataReference dataRef = new DataReference();
        dataRef.dataRef = newData;
        dataRef.key = insertKey;

        ChildReference splitLeft = null;
        ChildReference splitRight = null;
        KEY_TYPE splitKey = null;

        while (true) {
            // Step 1 尝试插入
            int insertIndex = computeInsertIndex(node, insertKey);

            if (root.getChildrenRefs().size() == 0
                    && splitLeft == null && splitRight == null) {// 针对只有一个RootNode节点的特殊情况
                node.getKeys().add(insertIndex, insertKey);
                ((RootNode) node).getDataRefs().add(insertIndex, newData);
            } else {  // 一般情况
                if (node instanceof LeafNode) {
                    ((LeafNode) node).insert(dataRef, insertIndex);
                } else if (node instanceof InnerNode) {
                    insertIndex = computeInsertIndex(node, splitKey);
                    ((InnerNode) node).insert(splitKey, splitLeft, splitRight, insertIndex);
                }
            }

            // Step 2判断插入后是否需要分裂节点
            if (node.isOverflow()) {
                int midIndex = keys.size() / 2 - 1;
                Node left = null;
                Node right = null;

                if (node instanceof RootNode
                        && root.getChildrenRefs().size() == 0 && splitLeft == null && splitRight == null) { // 针对只有一个RootNode节点的特殊情况
                    //split node
                    left = new LeafNode<KEY_TYPE, DATA_TYPE>(node, this);
                    right = new LeafNode<KEY_TYPE, DATA_TYPE>(node, this);

                    List<DATA_TYPE> dataRefs = ((RootNode) node).getDataRefs();
                    List<DATA_TYPE> leftDataRefs = ((LeafNode) left).getDataRefs();
                    List<DATA_TYPE> rightDataRefs = ((LeafNode) right).getDataRefs();

                    for (int i = 0; i <= midIndex; i++) {
                        left.getKeys().add(keys.get(i));
                        leftDataRefs.add(dataRefs.get(i));

                        right.getKeys().add(keys.get(i + midIndex));
                        rightDataRefs.add(dataRefs.get(i + midIndex));
                    }
                } else if (node instanceof InnerNode) {
                    //split node
                    left = new InnerNode<KEY_TYPE>(node, this);
                    right = new InnerNode<KEY_TYPE>(node, this);

                    List<Node> children = ((InnerNode) node).getChildrenRefs();
                    List<Node> leftChildren = ((InnerNode) left).getChildrenRefs();
                    List<Node> rightChildren = ((InnerNode) right).getChildrenRefs();

                    for (int i = 0; i <= midIndex; i++) {
                        left.getKeys().add(keys.get(i));
                        leftChildren.add(children.get(i));

                        right.getKeys().add(keys.get(i + midIndex));
                        rightChildren.add(children.get(i + midIndex));
                    }
                } else if (node instanceof LeafNode) {
                    //split node
                    left = new LeafNode<KEY_TYPE, DATA_TYPE>(node, this);
                    right = new LeafNode<KEY_TYPE, DATA_TYPE>(node, this);

                    List<DATA_TYPE> dataRefs = ((LeafNode) node).getDataRefs();
                    List<DATA_TYPE> leftDataRefs = ((LeafNode) left).getDataRefs();
                    List<DATA_TYPE> rightDataRefs = ((LeafNode) right).getDataRefs();

                    for (int i = 0; i <= midIndex; i++) {
                        left.getKeys().add(keys.get(i));
                        leftDataRefs.add(dataRefs.get(i));

                        right.getKeys().add(keys.get(i + midIndex));
                        rightDataRefs.add(dataRefs.get(i + midIndex));
                    }
                }

                if (node instanceof RootNode) {
                    root = new RootNode<KEY_TYPE, DATA_TYPE>(null, this);
                    node.setParent(root);
                }

                left.setParent(node);
                right.setParent(node);

                splitKey = keys.get(midIndex + 1);
                splitLeft = new ChildReference(left.getKeys().get(1), left);
                splitRight = new ChildReference(right.getKeys().get(1), right);

                node = node.getParent();
                // assert splitKey !=null && splitLeft != null && splitRight != null

            } else { // 不需要分裂，插入完成
                return true;
            }
        }

    }

    public boolean delete(KEY_TYPE key) {
        return false;
    }

    public void dump() {
        root.dump();
    }

    /**
     * 根据searchKey找到对应BTree的Node
     * 如果查询命中则返回对应LefNode，否则返回插入位置LefNode
     *
     * @param node
     * @param searchKey
     * @return
     */
    private Node innerGet(Node node, KEY_TYPE searchKey) {
        while (node != null) {
            // leafNode OR single node BTree
            if (node instanceof LeafNode || hasSingleNode()) {
                return node;
            }
            // InnerNode
            // B-Tree性质保证至少有1个key, 2个children
            List<KEY_TYPE> keys = node.getKeys();
            List<Node> children = ((InnerNode) node).getChildrenRefs();

            //InnerNode.keys[0] 作为一个guard值，其小于任何给定的searchKey
            //根据InnerNode中的定义：childrenRefs[i]指向的Node中的所有keys >= keys[i]
            if (comparator.compare(searchKey, keys.get(1)) > 0) { // searchKey < keys[1]
                // 根据上面的定义 keys[0] < all childrenRefs[i].keys  &&  MIN == keys[0]  < searchKey
                // 得出结论： MIN == keys[0]  < searchKey -->  childrenRefs[i].keys < keys[1]
                // 所以 searchKey可能存在与 childrenRefs[0]指向的节点中，返回 childrenRefs[0]
                node = children.get(0);
                continue;
            }

            if (comparator.compare(searchKey, keys.get(keys.size() - 1)) >= 0) {   // searchKey >= keys[keys.size() - 1 ]
                node = children.get(keys.size() - 1);
                continue;
            }

            //找到keys[i] <= searchKey < keys[i+1],返回childrenRefs[i]
            for (int i = 1; i < keys.size() - 1; i++) {
                if (comparator.compare(searchKey, keys.get(i)) >= 0 && comparator.compare(searchKey, keys.get(i + 1)) < 0) {
                    node = children.get(i);
                    continue;
                }
            }
        }
        throw new IllegalStateException("一定会在BTree中找到一个Node:或者是data对应的节点，或者是可以插入这个data的节点");
    }

    /**
     * 当前B+ Tree是否只有一个Node即RootNode节点
     *
     * @return
     */
    private boolean hasSingleNode() {
        return root.getChildrenRefs().size() == 0;
    }

    private static class IntegerComparator implements Comparator<Integer> {

        public int compare(Integer o1, Integer o2) {
            if (o1 > o2) {
                return 1;
            }
            if (o1 < o2) {
                return -1;
            }
            return 0;
        }
    }


}
