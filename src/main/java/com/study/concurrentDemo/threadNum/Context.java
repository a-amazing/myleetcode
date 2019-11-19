package com.study.concurrentDemo.threadNum;

/**
 * @author wangyi
 * @date 2019/11/19
 */
public class Context {
    public static void main(String[] args) throws InterruptedException {
        SynchronizedDemo synchronizedDemo = new SynchronizedDemo();
        synchronizedDemo.count();
    }
}
