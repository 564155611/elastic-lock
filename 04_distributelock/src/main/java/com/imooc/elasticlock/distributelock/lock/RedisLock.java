package com.imooc.elasticlock.distributelock.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class RedisLock implements AutoCloseable {
    private RedisTemplate<String, String> redisTemplate;
    private String key;
    private String value = UUID.randomUUID().toString();
    private Integer expiration;
    private Integer retryInterval = -1;
    private LockStatus lockStatus = LockStatus.UNLOCK;

    public RedisLock(RedisTemplate<String, String> redisTemplate, String key, Integer expiration) {
        this(redisTemplate, key, expiration, -1);
    }
    public RedisLock(RedisTemplate<String, String> redisTemplate, String key, Integer expiration,Integer retryInterval) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.expiration = expiration;
        this.retryInterval = retryInterval;
    }

    public void execute(Consumer<Boolean> c) {
        try{
            Boolean lock = this.lock();
            c.accept(lock);
        }finally {
            this.unlock();
        }
    }

    public Boolean lock() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Boolean lock = valueOperations.setIfAbsent(key, value, Duration.ofMillis(expiration));
        if (lock) {
            this.lockStatus = LockStatus.LOCKED;
            return lock;
        }
        if (retryInterval > 0) {
            log.info("获取锁失败,正在重新获取...");
            while (!lock) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock = valueOperations.setIfAbsent(key, value, Duration.ofSeconds(30));
            }
            this.lockStatus = LockStatus.LOCKED;
        }
        return lock;
    }

    @Override
    public void close() throws Exception {
        unlock();
    }

    public void unlock() {
        if (LockStatus.LOCKED.equals(this.lockStatus)) {
            String script =
                    "if redis.call(\"get\",KEYS[1])==ARGV[1] then\n" +
                            "   return redis.call(\"del\",KEYS[1])\n" +
                            "else\n" +
                            "   return 0\n" +
                            "end";
            RedisScript<Boolean> redisScript = RedisScript.of(script, Boolean.class);
            Boolean execute = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
            log.info("释放锁:{}",execute);
        }
    }

    public static enum LockStatus {
        LOCKED, UNLOCK;
    }
}
