package com.imooc.elasticlock.distributelock.demo;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CuratorExample {
    private static String address = "192.168.12.171:2181,192.168.12.172:2181,192.168.12.173:2181/lock";

    public void client1() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(address, 5000, 5000, retryPolicy);
        client.start();
    }

    public void client2() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(address).sessionTimeoutMs(5000).connectionTimeoutMs(5000).retryPolicy(retryPolicy).namespace("base").build();
        client.start();
    }

    public void create(CuratorFramework client) {
        try {
            client.create().creatingParentContainersIfNeeded() // 递归创建所需父节点
                    .withMode(CreateMode.PERSISTENT) // 创建类型为持久节点
                    .forPath("/nodeA", "init".getBytes()); // 目录及内容
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(CuratorFramework client) {
        try {
            client.delete().guaranteed()  // 强制保证删除
                    .deletingChildrenIfNeeded() // 递归删除子节点
                    .withVersion(10086) // 指定删除的版本号
                    .forPath("/nodeA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(CuratorFramework client) throws Exception {
        byte[] bytes = client.getData().forPath("/nodeA");
        System.out.println(new String(bytes));
    }

    public void getStat(CuratorFramework client) throws Exception {
        Stat stat = new Stat();
        client.getData().storingStatIn(stat).forPath("/nodeA");
    }

    public void setData(CuratorFramework client) throws Exception {
        client.setData().withVersion(10086) // 指定版本修改
                .forPath("/nodeA", "data".getBytes());
    }

    public void transactionOp(CuratorFramework client) throws Exception {
        client.inTransaction().check().forPath("/nodeA")
                .and()
                .create().withMode(CreateMode.EPHEMERAL).forPath("/nodeB", "init".getBytes())
                .and()
                .create().withMode(CreateMode.EPHEMERAL).forPath("/nodeC", "init".getBytes())
                .and()
                .commit();
    }

    public void checkExists(CuratorFramework client) throws Exception {
        client.checkExists() // 检查是否存在
                .forPath("/nodeA");
        client.getChildren().forPath("/nodeA"); // 获取子节点的路径
    }

    public void asyncOp(CuratorFramework client) throws Exception {
        Executor executor = Executors.newFixedThreadPool(2);
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground((curatorFramework, curatorEvent) -> {
                    System.out.println(String.format("eventType:%s,resultCode:%s", curatorEvent.getType(), curatorEvent.getResultCode()));
                }, executor)
                .forPath("path");
    }

}
