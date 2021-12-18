package com.imooc.elasticlock.distributelock.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class RedissonLock {

    @Autowired
    RedissonClient redissonClient;

    /*
    redisson-spring-boot-starter自动向容器中加入了这些bean:
    RedissonClient/RedissonRxClient/RedissonReactiveClient/RedisTemplate/ReactiveRedisTemplate
    @Bean
    RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("192.168.12.161:7617");
        return Redisson.create(config);
    }*/

    public void execute(String businessKey, Consumer<RLock> c) {
        RLock lock = redissonClient.getLock("/" + businessKey);
        lock.lock(30, TimeUnit.SECONDS);
        log.info("{}获得了锁", Thread.currentThread().getName());
        try {
            c.accept(lock);
        } finally {
            lock.unlock();
            log.info("{}释放了锁", Thread.currentThread().getName());
        }
    }
}
