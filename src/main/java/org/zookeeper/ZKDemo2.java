package org.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author Viper
 * @description
 * @date 2021/3/31
 */

public class ZKDemo2 {
    private ZooKeeper zooKeeper;

    @Before
    public void cerate2() {

    }

    @Test
    public void create1() {
        CountDownLatch count = new CountDownLatch(1);
        try {
            zooKeeper = new ZooKeeper("192.168.1.108:2181", 2000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == KeeperState.SyncConnected) {
                        System.out.println("connect success ~!");
                        count.countDown();
                    }
                    try {
                        count.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = null;
        try {
            if (zooKeeper != null) {
                s = zooKeeper.create("/spy1", "haha".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                System.out.println("zk 为连接");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(s);

    }

    @After
    public void create3() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}