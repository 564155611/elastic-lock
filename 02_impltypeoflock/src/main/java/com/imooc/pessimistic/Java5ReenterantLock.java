package com.imooc.pessimistic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Java5ReenterantLock {
    private int i;
    ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Java5ReenterantLock test = new Java5ReenterantLock();
        ExecutorService es = Executors.newFixedThreadPool(50);
        CountDownLatch cdl = new CountDownLatch(5000);
        for (int i = 0; i < 5000; i++) {
            es.execute(() -> {
                test.reentrantLock.lock();
                test.i++;
                test.reentrantLock.unlock();
                cdl.countDown();
            });
        }
        es.shutdown();
        cdl.await();
        System.out.println("执行完成后, i=" + test.i);
    }
}
