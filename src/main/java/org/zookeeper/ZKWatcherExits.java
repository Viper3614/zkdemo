package org.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Viper
 * @description
 * @date 2021/3/31
 */

public class ZKWatcherExits {
    CountDownLatch count = new CountDownLatch(1);
    ZooKeeper zooKeeper;

    @Test
    public void test() throws Exception {
        zooKeeper = new ZooKeeper("192.168.1.108:2181", 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接成功");
                    count.countDown();
                    System.out.println("path = " + event.getPath() + " , eventType = " + event.getType());
                }
            }

        });
        count.await();


        //1. 使用client连接server时的watcher对象
        // Stat exists = zooKeeper.exists("/viper", true);

        // 2. 使用exists的自定义的watcher对象
        zooKeeper.exists("/viper", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("exits 的自定义watcher对象");
                System.out.println("path=" + event.getPath() + ",type=" + event.getType());
                if (event.getType() == EventType.NodeDataChanged) {
                    try {
                        byte[] vipers = zooKeeper.getData("/viper", this, null);  // watcher是一次性的.如果监听的节点路径发生变化,再次重新监听
                        System.out.println(new String(vipers));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (event.getType() == EventType.NodeCreated) {
                    try {
                        System.out.println(new String(zooKeeper.getData("/viper", this, null)));
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (event.getType() == EventType.NodeDeleted) {
                    try {
                        System.out.println(zooKeeper.exists("/viper", this));
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread.sleep(5000000);
        zooKeeper.close();
        System.out.println("end~");
    }


}