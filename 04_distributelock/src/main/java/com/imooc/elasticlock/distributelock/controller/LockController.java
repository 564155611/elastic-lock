package com.imooc.elasticlock.distributelock.controller;

import com.imooc.elasticlock.distributelock.entity.DistributeLock;
import com.imooc.elasticlock.distributelock.lock.RedisLock;
import com.imooc.elasticlock.distributelock.lock.ZkLock;
import com.imooc.elasticlock.distributelock.mapper.DistributeLockMapper;
import com.imooc.elasticlock.distributelock.util.ZkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
public class LockController {
    private Lock lock = new ReentrantLock();
    @Autowired
    private DistributeLockMapper mapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @GetMapping("singlelock")
    public String singleLock() {
        System.out.println("进入了方法...");
        try {
            lock.lock();
            System.out.println("我拿到了锁");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return "singlelock";
    }

    @GetMapping("databaselock")
    @Transactional
    public String databaseLock(HttpServletRequest request) {
        int port = request.getServerPort();
        System.out.println(port + ": 进入了方法");
        DistributeLock distributelock = mapper.selectLock("distributelock");
        System.out.println(distributelock);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "databaselock";
    }

    @GetMapping("redislock")
    public String redislock(HttpServletRequest request) {
        int port = request.getServerPort();
        System.out.println(port + ": 进入了方法");
        String key = "redisKey";
        RedisLock redisLock = new RedisLock(redisTemplate, key, 30000, 1000);
        redisLock.execute(lock -> {
            if (lock) {
                System.out.println(port + ": 处理业务方法...");
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return "redislock";
    }

    @GetMapping("zklock")
    public String zkLock(HttpServletRequest request){
        int port = request.getServerPort();
        ZkLock zkLock = new ZkLock(ZkUtils.getZk(),
                "order",Thread.currentThread().getName());
        zkLock.execute(lock->{
            System.out.println(port + ": 处理业务方法...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return "zklock";
    }
}
