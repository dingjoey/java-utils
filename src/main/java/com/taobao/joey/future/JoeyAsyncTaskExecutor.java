package com.taobao.joey.future;

import java.util.concurrent.Callable;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: java-utils
 * User: qiaoyi.dingqy
 * Date: 13-7-3
 * Time: 下午3:39
 * <p/>
 */

public interface JoeyAsyncTaskExecutor {

    /**
     * 发起执行异步任务，并且同步获取异步任务的执行结果
     *
     * @param callable
     * @return
     */
    Object invokeWithSync(Callable callable) throws InterruptedException;

    /**
     * 发起执行异步任务；通过Future同步获取异步任务的执行结果
     *
     * @param callable
     * @return
     */
    JoeyFuture invokeWithFuture(Callable callable);

    /**
     * 发起执行异步任务；通过FutureListener异步获取异步任务的执行结果
     *
     * @param callable
     * @param listener
     * @return
     */
    JoeyFuture invokeWithFutureCallback(Callable callable, JoeyFuture.JoeyFutureListener listener);
}
