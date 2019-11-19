package com.study.concurrentDemo.threadNum;

/**
 * @author wangyi
 * @date 2019/11/19
 */
public class SynchronizedDemo {

    long count = 0;

    public void count() throws InterruptedException {
        for (int i = 0; i < 10000000; i++) {
//            countPlus();
            count++;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000000; i++) {
//                    synchronized (SynchronizedDemo.this) {
                        count++;
//                    }
                }

            }
        });
        thread.start();
        thread.join();
        System.out.println(count);
    }

    private  void countPlus(){
        count++;
    }
}
