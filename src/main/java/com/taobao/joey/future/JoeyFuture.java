package com.taobao.joey.future;

import java.util.EventListener;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: ����2:28
 * <p/>
 * ����ͬ��/�첽��ȡ�첽�����ִ��״̬�ͽ��
 * <p/>
 * <p/>
 * <p/>
 * JoeyFuture�Ĵ�����ʹ��������JoeyAsyncTaskExecutor
 * <p/>
 * ����ͬ����ȡ�첽����Ľ��ʹ�÷�ʽ:
 * <p> ��ѯ��ʽ
 * <blockquote><pre>
 *     JoeyFuture f = client.invokeWithFuture(appRequest);
 *     while (!f.isDone) {
 *         doSomeThingElse();
 *     }
 *     Object appResp = f.get(0L);
 * </pre></blockquote>
 * <p/>
 * <p>��������������ʽ
 * <blockquote><pre>
 *     JoeyFuture f = client.invokeWithFuture(appRequest);
 *     doSomeThingElse();
 *     Object appResp = f.get(2000L);   // ��������ȡ�첽������
 *     // Object appResp = f.get(); // ������ȡ�첽������
 * </pre></blockquote>
 * �첽��ȡ�첽����Ľ��ʹ�÷�ʽ:
 * <blockquote><pre>
 *   JoeyFuture f = client.invokeWithFutureCallback(appRequest, listeners);
 * </pre></blockquote>
 */
public interface JoeyFuture<V> {

    // part A: ͬ����ȡ�첽����ִ�н���Ľӿں���
    // Wait for the asynchronous operation to end, return the result
    V get() throws InterruptedException;
    // Wait for the asynchronous operation to end with the specified timeout
    V get(long timeout) throws InterruptedException, FutureTimeoutException;
    boolean isDone();

    // part B: �첽��ȡ�첽����ִ�н���Ľӿں���
    void addListener(JoeyFutureListener listener);
    void removeListener(JoeyFutureListener listener);
    /**
     * �첽����ִ����Ϻ��Callback Listener
     */
    interface JoeyFutureListener extends EventListener {
        void operationComplete(JoeyFuture future);
    }

    /**
     * ��ͬ��������ʽ��ȡ�첽����ʱ�׳���exception
     * timeout exveption
     */
    class FutureTimeoutException extends Exception {
    }
}
