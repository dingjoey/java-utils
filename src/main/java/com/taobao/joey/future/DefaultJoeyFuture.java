package com.taobao.joey.future;

import java.util.List;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: 下午2:51
 */
public class DefaultJoeyFuture<V> implements JoeyFuture {
    private final Object lock;
    private volatile boolean done; // 避免使用synchronized
    private V result;
    private List<JoeyFutureListener> listeners;

    public DefaultJoeyFuture() {
        this.lock = this;
    }

    /**
     * 阻塞等待异步任务执行完毕获取异步任务的结果
     * 阻塞线程可以被Thread.interrupt中断
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
     * 为阻塞等待设置超时
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
     * 同步非阻塞查询异步任务执行状态
     *
     * @return
     */
    public boolean isDone() {
        // done is volatile, no synchronized is needed
        return done;
    }

    /**
     * 异步任务执行完毕后需要调用此函数设置执行完毕状态（结果）
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
     * 当异步任务执行完毕后回调JoeyFutureListener，起到异步获取异步任务执行结果的效果
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
