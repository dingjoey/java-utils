package com.taobao.joey.future;

import java.util.concurrent.Callable;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: ����3:39
 * <p/>
 */

public interface JoeyAsyncTaskExecutor {

    /**
     * ����ִ���첽���񣬲���ͬ����ȡ�첽�����ִ�н��
     *
     * @param callable
     * @return
     */
    Object invokeWithSync(Callable callable) throws InterruptedException;

    /**
     * ����ִ���첽����ͨ��Futureͬ����ȡ�첽�����ִ�н��
     *
     * @param callable
     * @return
     */
    JoeyFuture invokeWithFuture(Callable callable);

    /**
     * ����ִ���첽����ͨ��FutureListener�첽��ȡ�첽�����ִ�н��
     *
     * @param callable
     * @param listener
     * @return
     */
    JoeyFuture invokeWithFutureCallback(Callable callable, JoeyFuture.JoeyFutureListener listener);
}
