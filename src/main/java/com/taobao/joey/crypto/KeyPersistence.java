package com.taobao.joey.crypto;

import java.security.Key;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-29
 * Time: ����4:35
 */
public class KeyPersistence {

    /**
     *
     * @param key
     */
    public static void dumpKeyInfo(Key key) {
        /**
         *������Կ���ⲿ������ʽ��
         * �� Java �����֮����Ҫ��Կ�ı�׼��ʾ��ʽʱ�Լ�����Կ���䵽����ĳЩ����ʱʹ�á�
         */
        System.out.println("Encoded : " + key.getEncoded());
        /**
         * �����ѱ�����Կ�ĸ�ʽ������
         */
        System.out.println("Format : " + key.getFormat());

        System.out.println("Algorithm : " + key.getAlgorithm());
    }
}
