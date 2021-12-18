package com.imooc.elasticlock.distributelock.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZkUtils {
    private static String address = "192.168.12.171:2181,192.168.12.172:2181,192.168.12.173:2181/lock";
    private ZkUtils(){}
    public static ZooKeeper getZk() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(address, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    Event.EventType type = event.getType();
                    Event.KeeperState state = event.getState();
                    try {
                        System.out.println(new ObjectMapper().writeValueAsString(event));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    switch (state) {
                        case Unknown:
                            break;
                        case Disconnected:
                            log.info("连接断开了");
                            break;
                        case NoSyncConnected:
                            break;
                        case SyncConnected:
                            log.info("已连接");
                            countDownLatch.countDown();
                            break;
                        case AuthFailed:
                            log.info("认证失败");
                            break;
                        case ConnectedReadOnly:
                            log.info("连接到了一个只读的服务器");
                            break;
                        case SaslAuthenticated:
                            log.info("Sasl认证通过");
                            break;
                        case Expired:
                            log.info("会话已过期");
                            break;
                        case Closed:
                            log.info("连接已关闭");
                            break;
                    }
                    switch (type) {
                        case None:
                            break;
                        case NodeCreated:
                            log.info("{}路径下创建了节点",event.getPath());
                            break;
                        case NodeDeleted:
                            log.info("删除了节点{}",event.getPath());
                            break;
                        case NodeDataChanged:
                            log.info("{}节点数据发生了变化",event.getPath());
                            break;
                        case NodeChildrenChanged:
                            log.info("{}子节点发生了变化",event.getPath());
                            break;
                        case DataWatchRemoved:
                            log.info("数据监听已失效");
                            break;
                        case ChildWatchRemoved:
                            log.info("子节点监听已失效");
                            break;
                        case PersistentWatchRemoved:
                            log.info("持久化监听已失效");
                            break;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }
}
