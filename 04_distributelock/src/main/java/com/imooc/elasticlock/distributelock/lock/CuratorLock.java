package com.imooc.elasticlock.distributelock.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class CuratorLock {
    private static String address = "192.168.12.171:2181,192.168.12.172:2181,192.168.12.173:2181/lock";

    @Bean(name = "curatorClient", initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        return CuratorFrameworkFactory.newClient(address, 5000, 5000, retryPolicy);
    }

    public void execute(String businessKey, Consumer<InterProcessMutex> c) {
        CuratorFramework client = curatorClient();
        InterProcessMutex lock = new InterProcessMutex(client, "/" + businessKey);
        try {
            if (lock.acquire(30, TimeUnit.SECONDS)) {
                try {
                    log.info("{}获得了锁",Thread.currentThread().getName());
                    c.accept(lock);
                } finally {
                    lock.release();
                    log.info("{}释放了锁");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
