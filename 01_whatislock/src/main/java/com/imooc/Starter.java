package com.imooc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Starter {
    public static void main(String[] args) {
        Cabinet cabinet = new Cabinet();
        ExecutorService es = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            final int storeNumber = i;
            es.execute(() -> {
                User user = new User(cabinet, storeNumber);
                synchronized (cabinet) {
                    user.useCabinet();
                    System.out.println("我是用户" + storeNumber + ",我存储的数字是: " + cabinet.getStoreNumber());
                }
            });
        }
        es.shutdown();
    }
}
