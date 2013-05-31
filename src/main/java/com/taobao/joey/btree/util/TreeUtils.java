package com.taobao.joey.btree.util;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-27
 * Time: ионГ10:25
 */
public class TreeUtils {

    static private final String SPACES =
                    "                                " +
                    "                                " +
                    "                                " +
                    "                                ";

    /**
     * For tree dumper.
     */
    public static String indent(int nSpaces) {
        return SPACES.substring(0, nSpaces);
    }
}
