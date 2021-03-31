package org.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Viper
 * @description 使用zookeeper生成 分布式锁
 * @date 2021/3/31
 */

public class MyLock {
    private static final String LOCK_ROOT_PATH = "/locks";
    private static final String LOCK_NODE_PATH = "lock_";
    private String lockPath;
    private String ip = "192.168.1.108:2181";
    private ZooKeeper zooKeeper;
    private CountDownLatch count = new CountDownLatch(1);

    private Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == EventType.NodeDeleted) {   // 监控节点删除事件
                synchronized (this) {
                    notifyAll(); //通知该client 线程状态被唤醒，开始争抢锁
                }
            }
        }
    };

    public MyLock() {
        try {
            zooKeeper = new ZooKeeper(ip, 5000, new Watcher() {
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
            });
            count.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MyLock myLock = new MyLock();
        myLock.getMyLock();
    }

    // 获取锁
    public void getMyLock() {
        try {
            createLock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建锁
    public void createLock() throws Exception {
        // 判断  LOCK_ROOT_PATH 是否存在
        Stat stat = zooKeeper.exists(LOCK_ROOT_PATH, false);
        if (stat == null) {
            zooKeeper.create(LOCK_ROOT_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // 创建临时有序子节点
        lockPath = zooKeeper.create(LOCK_ROOT_PATH + "/" + LOCK_NODE_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("节点创建成功：" + lockPath);
    }

    //尝试获取锁
    public void attemptLock() throws Exception {
        List<String> list = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
        Collections.sort(list);
        int index = list.indexOf(lockPath.substring(LOCK_ROOT_PATH.length() + 1));
        if (index == 0) { // 如果集合中index下标为0，表示锁节点可以获取锁
            System.out.println("获取锁成功");
            return;
        } else {
            String path = list.get(index - 1);// 上一个节点路径
            Stat stat = zooKeeper.exists(LOCK_ROOT_PATH + "/" + path, watcher);
            if (stat == null) { // stat为null，表示上一个节点已经删除掉了临时节点
                attemptLock();  // 获取锁
            } else {
                synchronized (watcher) {  //
                    watcher.wait();
                }
                attemptLock();
            }
        }
    }

    //释放锁
    public void releaseLock() {
        try {
            zooKeeper.delete(this.lockPath, -1);
            zooKeeper.close();
            System.out.println("临时节点已删除，锁已经释放：" + this.lockPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}