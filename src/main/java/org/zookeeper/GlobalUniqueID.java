package org.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

import java.util.concurrent.CountDownLatch;

/**
 * @author Viper
 * @description  使用zookeeper生成全局唯一id
 * @date 2021/3/31
 */

public class GlobalUniqueID implements Watcher {
    CountDownLatch count = new CountDownLatch(1);
    String defaultPath = "/uniqueId";
    ZooKeeper zooKeeper;
    private String ip = "192.168.1.108:2181";

    public GlobalUniqueID() {
        //创建zk连接
        try {
            zooKeeper = new ZooKeeper(ip, 5000, this);
            count.await(); // 阻塞线程,等待连接创建成功
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GlobalUniqueID id = new GlobalUniqueID();
        for (int i = 0; i < 5; i++) {
            String uniqueID = id.getUniqueID();
            System.out.println(uniqueID);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if (event.getState() == KeeperState.SyncConnected) {
                System.out.println("连接成功");
                count.countDown();
            } else if (event.getState() == KeeperState.Disconnected) {
                System.out.println("连接断开");
            } else if (event.getState() == KeeperState.Expired) {
                System.out.println("连接超时");
            } else if (event.getState() == KeeperState.AuthFailed) {
                System.out.println("认证失败");
            }
            count.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 创建临时有序的节点
     */
    public String getUniqueID() {
        String path = "";
        try {
            path = zooKeeper.create(defaultPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return path.substring(9);
    }
}