package org.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.junit.jupiter.api.Test;

/**
 * Hello world!
 */

public class ZKDemo {
    @Test
    public void test() throws Exception {
        // 1.创建zk连接
        ZooKeeper zooKeeper = new ZooKeeper("192.168.1.108:2181", 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState().getIntValue() == KeeperState.SyncConnected.getIntValue()) {
                    System.out.println("zk连接成功");
                }
            }
        });

        //2，创建父节点
        String path = zooKeeper.create("/viper",
                "nodeValue".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,  // 当前节点以word方式进行授权 源码：ANYONE_ID_UNSAFE = new Id("world", "anyone");
                CreateMode.PERSISTENT);//节点类型，当前节点是一个持久化节点
        System.out.println("路径：" + path);

    }

}
