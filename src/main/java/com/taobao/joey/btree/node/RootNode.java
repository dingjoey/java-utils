package com.taobao.joey.btree.node;

import com.taobao.joey.btree.BTreeImpl;
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
public class RootNode<KEY_TYPE, DATA_TYPE> extends InnerNode<KEY_TYPE> {
    // 特殊的只有一个节点的B+Tree树情况下存在
    private List<DATA_TYPE> dataRefs = new ArrayList<DATA_TYPE>();

    public RootNode(Node parent, BTreeImpl tree) {
        super(parent, tree);
    }

    /**
     * dump BTree node into String
     *
     * @return
     */
    @Override
    protected String dumpString(int nSpaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("<rootNode>");
        for (int i = 0; i < keys.size(); i++) {
            sb.append('\n');
            sb.append(TreeUtils.indent(nSpaces + 1));
            if (i == 0) {
                sb.append("<key>MIN_LEY</key>");
            } else {
                sb.append("<key>").append(keys.get(i).toString()).append("</key>");
            }

            sb.append('\n');
            sb.append(TreeUtils.indent(nSpaces + 1));
            if (childrenRefs.size() > 0) {
                sb.append(childrenRefs.get(i).dumpString(nSpaces + 1));
            } else if (dataRefs.size() > 0) {
                sb.append("<dataRef>").append(dataRefs.get(i).toString()).append("</dataRef>");
            }
        }
        sb.append('\n');
        sb.append("</rootNode>");
        return sb.toString();

    }

    public List<DATA_TYPE> getDataRefs() {
        return dataRefs;
    }
}
