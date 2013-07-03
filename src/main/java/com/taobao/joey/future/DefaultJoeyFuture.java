package com.taobao.joey.future;

import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: ����2:51
 */
public class DefaultJoeyFuture<V> implements JoeyFuture {
    private final Object lock;
    private volatile boolean done; // ����ʹ��synchronized
    private V result;
    private List<JoeyFutureListener> listeners;

    public DefaultJoeyFuture() {
        this.lock = this;
    }

    /**
     * �����ȴ��첽����ִ����ϻ�ȡ�첽����Ľ��
     * �����߳̿��Ա�Thread.interrupt�ж�
     *
     * @return
     * @throws InterruptedException
     */
    public Object get() throws InterruptedException {
        synchronized (lock) {
            while (!done) {
                lock.wait();
            }
        }
        return result;
    }

    /**
     * Ϊ�����ȴ����ó�ʱ
     *
     * @param timeout
     * @return
     * @throws InterruptedException
     * @throws FutureTimeoutException
     */
    public Object get(long timeout) throws InterruptedException, FutureTimeoutException {
        if (timeout <= 0) return get();

        long endTime = System.currentTimeMillis() + timeout;
        long toWaitTime = timeout;

        synchronized (lock) {
            while (!done && toWaitTime > 0) {
                lock.wait(timeout);
                toWaitTime = endTime - System.currentTimeMillis();
            }
        }

        if (toWaitTime <= 0) {
            throw new FutureTimeoutException();
        } else {
            //assert done == true;
            return result;
        }
    }

    /**
     * ͬ����������ѯ�첽����ִ��״̬
     *
     * @return
     */
    public boolean isDone() {
        // done is volatile, no synchronized is needed
        return done;
    }

    /**
     * �첽����ִ����Ϻ���Ҫ���ô˺�������ִ�����״̬�������
     *
     * @param r
     */
    public void setResult(V r) {
        synchronized (lock) {
            this.result = r;
            done = true;
            lock.notifyAll();
            notifyListeners();
        }
    }

    /**
     * ���첽����ִ����Ϻ�ص�JoeyFutureListener�����첽��ȡ�첽����ִ�н����Ч��
     */
    private void notifyListeners() {
        synchronized (lock) {
            for (JoeyFutureListener listener : listeners) {
                listener.operationComplete(this);
            }
        }
    }

    public void addListener(JoeyFutureListener listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void removeListener(JoeyFutureListener listener) {
        synchronized (lock) {
            listeners.remove(listener);
        }
    }


}
