package com.imooc.elasticlock.distributelock.lock;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Slf4j
public class ZkLock implements AutoCloseable,Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback {
    private ZooKeeper zk;
    private String key;
    private String threadId;
    private String fullPath;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public ZkLock(ZooKeeper zk,String key,String threadId) {
        Assert.notNull(zk);
        Assert.notEmpty(key);
        Assert.notEmpty(threadId);

        this.zk = zk;
        this.key = key;
        this.threadId = threadId;
    }

    public void execute(Consumer<ZkLock> c) {
        lock();
        c.accept(this);
        unlock();
    }

    public void lock() {
        zk.getData("/", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                ZkLock cb = (ZkLock) ctx;
                if (data == null || !new String(data, StandardCharsets.UTF_8).equals(threadId)) {
                    //创建锁
                    zk.create("/" + key,
                            "".getBytes(StandardCharsets.UTF_8),
                            ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.EPHEMERAL_SEQUENTIAL,
                            cb, ctx);
                }else{
                    countDownLatch.countDown();
                }
            }
        }, this);
        try {
            countDownLatch.await();
            log.info("{}:{}开始工作",threadId,fullPath);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unlock(){
        try {
            log.info(threadId + " over work");
            zk.delete(fullPath,-1);
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        unlock();
    }


    /**
     * StringCallback
     * @param rc
     * @param path
     * @param ctx
     * @param name
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null) {
            this.fullPath = name;
        }
        log.info("{}创建节点{}",threadId,fullPath);
        zk.getChildren("/", false, this, ctx);
    }

    /**
     * ChildrenCallback
     *
     * @param rc
     * @param path
     * @param ctx
     * @param children
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        if (stat == null) {
            throw new RuntimeException("状态异常");
        }
        Collections.sort(children);
        String currentChildName = fullPath.substring(path.length());
        int idx = children.indexOf(currentChildName);
        if (idx < 0) {
            log.error("当前线程的fullPath:{}", fullPath);
            throw new RuntimeException("当前线程的pfullPath不存在于children中");
        }
        if (idx == 0) {
            zk.setData("/", threadId.getBytes(StandardCharsets.UTF_8), -1, new StatCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, Stat stat) {
                    if (stat == null) {
                        throw new RuntimeException("设置目录数据失败");
                    }
                    countDownLatch.countDown();
                }
            }, ctx);
        }else {
            zk.exists(path + children.get(idx - 1), this, new StatCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, Stat stat) {
                    if (stat == null) {
                        //说明前面节点不存在
                        try {
                            zk.removeWatches(path, (Watcher) ctx, WatcherType.Any, false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        }
                        //父目录不会变化无需监控,获取children完成后利用children2Callback进行回调
                        zk.getChildren(path, false, (ChildrenCallback) ctx, ctx);
                    }
                }
            }, ctx);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {

            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/",false,this,this);
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }
}
