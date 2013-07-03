package com.taobao.joey.future;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: ����3:50
 */
public class DefaultJoeyAsyncTaskExecutor implements JoeyAsyncTaskExecutor {

    private final Executor executor;

    public DefaultJoeyAsyncTaskExecutor(Executor executor) {
        if (executor != null)
            this.executor = executor;
        else {
            this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                AtomicLong id = new AtomicLong(1L);

                public Thread newThread(Runnable r) {
                    String name = "JoeyAsyncTaskExecutor--thread--" + id.getAndIncrement();
                    Thread t = new Thread(r, name);
                    t.setDaemon(true);
                    return t;
                }
            });
        }
    }

    /**
     * ����ִ���첽���񣬲��һ�ȡ�첽�����ִ�н��
     *
     * @param callable
     * @return
     */
    public Object invokeWithSync(Callable callable) throws InterruptedException {
        JoeyAsyncTask task = new JoeyAsyncTask(callable);
        executor.execute(task);
        return task.get();
    }

    /**
     * ����ִ���첽����ͨ��Futureͬ����ȡ�첽�����ִ�н��
     *
     * @param callable
     * @return
     */
    public JoeyFuture invokeWithFuture(Callable callable) {
        JoeyAsyncTask task = new JoeyAsyncTask(callable);
        executor.execute(task);
        return task;
    }

    /**
     * ����ִ���첽����ͨ��FutureListener�첽��ȡ�첽�����ִ�н��
     *
     * @param callable
     * @param listener
     * @return
     */
    public JoeyFuture invokeWithFutureCallback(Callable callable, JoeyFuture.JoeyFutureListener listener) {
        JoeyAsyncTask task = new JoeyAsyncTask(callable);
        task.addListener(listener);
        executor.execute(task);
        return task;
    }
}
