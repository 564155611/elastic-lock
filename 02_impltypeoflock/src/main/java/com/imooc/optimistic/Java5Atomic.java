package com.imooc.optimistic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Java5Atomic {
    private AtomicInteger i = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        Java5Atomic test = new Java5Atomic();
        ExecutorService es = Executors.newFixedThreadPool(50);
        CountDownLatch cdl = new CountDownLatch(5000);
        for (int i = 0; i < 5000; i++) {
            es.execute(() -> {
                test.i.incrementAndGet();
                cdl.countDown();
            });
        }
        es.shutdown();
        cdl.await();
        System.out.println("执行完成后, i=" + test.i);
    }
}

