package org.zookeeper;

import org.apache.commons.mail.SimpleEmail;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author Viper
 * @description
 * @date 2021/3/31
 */

public class ZKWatcher implements Watcher {
    ZooKeeper zooKeeper;
    CountDownLatch count = new CountDownLatch(1);

    @Test
    public void test() {
        try {
            initZK();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initZK() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("192.168.1.108:2181", 2000, new ZKWatcher());
       // count.await();
        Thread.sleep(100000);
        long sessionId = zooKeeper.getSessionId();
        System.out.println("sessionID:" + sessionId);
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            System.out.println("connect success");
            count.countDown();
        } else if (event.getState() == Event.KeeperState.Disconnected) {
            System.out.println("disconnect success");
            // send email，短信给运维人员
            sendEmail();
        } else if (event.getState() == Event.KeeperState.Expired) {
            System.out.println("connect Expired");
        }
    }

    public void sendEmail(){
        SimpleEmail email = new SimpleEmail();
        try {
            // 发送电子邮件的邮件服务器地址
            email.setHostName("smtp.qq.com");
            email.setCharset("UTF-8");// 设置字符编码
            // 邮箱服务器身份验证  qhmiujfmbktpbged
            // qq客户端授权码获取方式： https://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=1001256
            email.setAuthentication("361457925@qq.com", "qhmiujfmbktpbged");
            // 设置发件人邮箱(与用户名保持一致) 并且 设置发件人昵称
            email.setFrom("361457925@qq.com","zookeeper disconnect");
            // 邮件主题
            email.setSubject("zookeeper disconnect," + new Date());
            // 邮件内容
            email.setMsg("hi 我是一个测试文本，请忽略！");
            // 收件人地址
            email.addTo("361457925@qq.com");
            // 邮件发送
            email.send();
            System.out.println("邮件发送成功！");

        } catch (Exception e ) {
            e.printStackTrace();
            System.err.println("邮件发送失败");
        }
    }
}