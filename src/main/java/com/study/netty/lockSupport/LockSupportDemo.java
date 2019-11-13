package com.study.netty.lockSupport;

import java.util.concurrent.locks.LockSupport;

/**
 * @author:wangyi
 * @Date:2019/11/13
 */
public class LockSupportDemo {
    public static void main(String[] args) throws InterruptedException {
//        LockSupport.unpark(Thread.currentThread());
//        LockSupport.park();
//        System.out.println("blocked!");
//        LockSupport.park();
//        System.out.println("blocked again!");
        test2();
    }

    public static void test2() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            private int count = 0;

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                long end = 0;

                while ((end - start) <= 1000) {
                    count++;
                    end = System.currentTimeMillis();
                }

                System.out.println("after 1 second.count=" + count);

                //等待或许许可
                LockSupport.park();
                System.out.println("thread over." + Thread.currentThread().isInterrupted());

            }
        });
        t.start();

        Thread.sleep(2000);
        t.interrupt();
        System.out.println("main over!");
    }
}
