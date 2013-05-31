package com.taobao.joey.crypto;

import java.security.Key;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-29
 * Time: 下午4:35
 */
public class KeyPersistence {

    /**
     *
     * @param key
     */
    public static void dumpKeyInfo(Key key) {
        /**
         *这是密钥的外部编码形式，
         * 在 Java 虚拟机之外需要密钥的标准表示形式时以及将密钥传输到其他某些部分时使用。
         */
        System.out.println("Encoded : " + key.getEncoded());
        /**
         * 这是已编码密钥的格式的名称
         */
        System.out.println("Format : " + key.getFormat());

        System.out.println("Algorithm : " + key.getAlgorithm());
    }
}
