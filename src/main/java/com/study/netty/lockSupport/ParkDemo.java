package com.study.netty.lockSupport;

import java.util.concurrent.locks.LockSupport;

/**
 * @author:wangyi
 * @Date:2019/11/15
 */
public class ParkDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                LockSupport.park();
                System.out.println(System.currentTimeMillis());
                System.out.println("获取permit,执行了!");
                LockSupport.park();
                System.out.println(System.currentTimeMillis());
                System.out.println("再次获取permit,执行了!");
            }
        };
        t.start();
        LockSupport.unpark(t);
        Thread.sleep(2000);
        LockSupport.unpark(t);
    }
}
