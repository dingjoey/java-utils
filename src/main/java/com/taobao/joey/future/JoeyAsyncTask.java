package com.taobao.joey.future;

import java.util.concurrent.Callable;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: ����4:08
 * �ѵ����ڽ��첽�����Future��������
 * JoeyAsycTask�͸���˹���
 */
public class JoeyAsyncTask<T> extends DefaultJoeyFuture implements Runnable, JoeyFuture {

    private final Callable callable;

    public JoeyAsyncTask(Callable callable) {
        this.callable = callable;
    }

    public void run() {
        Object result = null;
        try {
            result = callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setResult(result);
    }
}
