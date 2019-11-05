package com.study.concurrentDemo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author:wangyi
 * @Date:2019/11/4
 */
public class CountDownLatchDemo {
    public static void main(String[] args) {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(100);
        AtomicLong num = new AtomicLong(0);

        ExecutorService pool = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 100; i++) {
            pool.execute(() -> {
                try {
                    startSignal.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                num.getAndIncrement();
                System.out.println(Thread.currentThread().getName() + " is executing!");
                doneSignal.countDown();
            });
        }

        long start = System.currentTimeMillis();
        startSignal.countDown();
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(num.get());
        System.out.println(end - start);
        pool.shutdown();
    }
}
