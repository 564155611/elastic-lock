package com.imooc.elasticlock.distributelock.entity;

import lombok.Data;

/**
 * 分布式锁 
 * distribute_lock
 * @author fanx
 * @date 2021-12-13 18:08:27
 */
@Data
public class DistributeLock {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 业务码
     */
    private String businessCode;
}