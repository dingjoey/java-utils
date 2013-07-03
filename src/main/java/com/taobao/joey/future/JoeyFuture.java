package com.taobao.joey.future;

import java.util.EventListener;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: 下午2:28
 * <p/>
 * 用于同步/异步获取异步任务的执行状态和结果
 * <p/>
 * <p/>
 * <p/>
 * JoeyFuture的创建和使用依赖于JoeyAsyncTaskExecutor
 * <p/>
 * 两种同步获取异步任务的结果使用方式:
 * <p> 轮询方式
 * <blockquote><pre>
 *     JoeyFuture f = client.invokeWithFuture(appRequest);
 *     while (!f.isDone) {
 *         doSomeThingElse();
 *     }
 *     Object appResp = f.get(0L);
 * </pre></blockquote>
 * <p/>
 * <p>基于条件变量方式
 * <blockquote><pre>
 *     JoeyFuture f = client.invokeWithFuture(appRequest);
 *     doSomeThingElse();
 *     Object appResp = f.get(2000L);   // 非阻塞获取异步任务结果
 *     // Object appResp = f.get(); // 阻塞获取异步任务结果
 * </pre></blockquote>
 * 异步获取异步任务的结果使用方式:
 * <blockquote><pre>
 *   JoeyFuture f = client.invokeWithFutureCallback(appRequest, listeners);
 * </pre></blockquote>
 */
public interface JoeyFuture<V> {

    // part A: 同步获取异步任务执行结果的接口函数
    // Wait for the asynchronous operation to end, return the result
    V get() throws InterruptedException;
    // Wait for the asynchronous operation to end with the specified timeout
    V get(long timeout) throws InterruptedException, FutureTimeoutException;
    boolean isDone();

    // part B: 异步获取异步任务执行结果的接口函数
    void addListener(JoeyFutureListener listener);
    void removeListener(JoeyFutureListener listener);
    /**
     * 异步任务执行完毕后的Callback Listener
     */
    interface JoeyFutureListener extends EventListener {
        void operationComplete(JoeyFuture future);
    }

    /**
     * 当同步阻塞方式获取异步任务超时抛出此exception
     * timeout exveption
     */
    class FutureTimeoutException extends Exception {
    }
}
