package com.taobao.joey.test;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * User: qiaoyi.dingqy
 * Date: 13-5-16
 * Time: ионГ8:47
 */
public class SplitTest {

    public static void main(String[] args) {
        System.out.println("123".split(":")[0]);
        System.out.println("123:".split(":")[0]);
        System.out.println("123:345".split(":")[0]);
    }
}
