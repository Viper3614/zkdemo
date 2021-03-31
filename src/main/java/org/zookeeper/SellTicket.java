package org.zookeeper;

/**
 * @author Viper
 * @description 通过分布式锁来实现买票的过程
 * @date 2021/3/31
 */

public class SellTicket {

    public static void main(String[] args) {
        SellTicket sellTicket = new SellTicket();
        for (int i = 0; i < 20; i++) {
            sellTicket.sellTickWithLock();
            System.out.println(Thread.currentThread().getName() + "，卖票成功");
        }
    }

    public void sell() {
        System.out.println("卖票开始");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("卖票结束");
    }

    public void sellTickWithLock() {
        MyLock myLock = new MyLock();
        myLock.getMyLock();
        sell();
        myLock.releaseLock();
    }
}