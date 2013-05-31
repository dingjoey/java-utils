package com.taobao.joey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-4-27
 * Time: 下午2:45
 * To change this template use File | Settings | File Templates.
 * <p/>
 * 理解ExecutorService的
 * shutdown作用：组织Task提交到任务队列 ；
 * shutdownNow作用：通过interrupt终止真正执行任务的线程，返回任务队列中剩余的Task
 */
public class UsingExecutorService {
    private final int taskCnt = 100;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ArrayList<Future> results = new ArrayList<Future>(taskCnt);

    public static void main(String[] args) throws InterruptedException {
        UsingExecutorService es = new UsingExecutorService();

        Thread startThread = es.dispatchTask();
        Thread stopThread = es.stopExecutor();
        //Thread monitorThread = es.monitorTask();

        startThread.start();
        stopThread.start();
        //monitorThread.start();

        startThread.join();
        stopThread.join();

        //monitorThread.interrupt();

        //monitorThread.join();
    }

    public Thread dispatchTask() {
        Thread taskDispather = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < taskCnt; i++) {
                    results.add(executor.submit(new Task()));
                }
            }
        });
        taskDispather.setName("Thread-dispatchTask");
        return taskDispather;
    }

    public Thread stopExecutor() {
        Thread executorStopper = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // 1 阻止任务的提交
                executor.shutdown();
                System.out.println("disabling executor task submitting");

                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.out.println("executor is still running");
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }

                    // 2 停止正在执行的任务，返回任务队列剩下的
                    // ExecutorService shutdownNow通过向interrupt Woker线程实现
                    List<Runnable> toToTasks = executor.shutdownNow(); // Cancel currently executing tasks
                    System.out.println("shutdown executor brutely");
                    System.out.println("todotask size : " + toToTasks.size());

                    // Wait a while for tasks to respond to being cancelled
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");

                } catch (InterruptedException e) {
                    // (Re-)Cancel if current thread also interrupted
                    executor.shutdownNow();
                    // Preserve interrupt status
                    Thread.currentThread().interrupt();
                }

                System.out.println("[stopExecutor] shutdown executor");
            }
        });
        executorStopper.setName("Thread-stopExecutor");
        return executorStopper;
    }

    public Thread monitorTask() {
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    List<Future> currentResults = new ArrayList<Future>(results); //XXX 如何实现snapshot 迭代？
                    for (int i = 0; i < currentResults.size(); i++) {
                        System.out.println(i + " : " + currentResults.get(i).isDone());
                    }
                }
                System.out.println("[monitorThread] stop monitor");
            }
        });
        monitorThread.setName("Thread-monitorTask");
        return monitorThread;
    }

    protected static class IDGenerater {
        public static AtomicInteger next = new AtomicInteger(0);

        public static int next() {
            return next.getAndIncrement();
        }
    }

    private class Task implements Runnable {
        public int id = IDGenerater.next();

        @Override
        public void run() {
            try {
                for (int i = 0; i < 5000; i++) {
                    Thread.sleep(10);
                }
                System.out.println(id);
            } catch (InterruptedException e) {
                System.out.println("current task is being interrupted" + id);
            }
        }
    }

    ;

}
