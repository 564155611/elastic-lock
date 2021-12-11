package com.imooc.pessimistic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronizedLock {
    private int i;

    public static void main(String[] args) throws InterruptedException {
        SynchronizedLock test = new SynchronizedLock();
        ExecutorService es = Executors.newFixedThreadPool(50);
        CountDownLatch cdl = new CountDownLatch(5000);
        for (int i = 0; i < 5000; i++) {
            es.execute(() -> {
                synchronized (test) {
                    test.i++;
                }
                cdl.countDown();
            });
        }
        es.shutdown();
        cdl.await();
        System.out.println("执行完成后, i=" + test.i);
    }
}
