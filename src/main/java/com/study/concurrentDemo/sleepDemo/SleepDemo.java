package com.study.concurrentDemo.sleepDemo;

/**
 * @author wangyi
 * @date 2019/11/17
 */
public class SleepDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("当前时间是:" + System.currentTimeMillis());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    System.out.println("新线程被中断了!");
                    return;
                }
                System.out.println("新线程正常结束了!");
            }
        });
        thread.start();

        Thread.sleep(500);
        //Thread.interrupted()中断的是当前线程!
        thread.interrupt();
    }
}
