package com.study.designPattern.singleton;

/**
 * @author:wangyi
 * @Date:2019/8/28
 */
public class Singleton3 {
    private static volatile Singleton3 instance = null;

    private Singleton3() {
    }

    /**
     * double check
     * @return
     */
    public static Singleton3 getInstance() throws InterruptedException {
        if (instance == null) {
            Thread.sleep(500);
            synchronized (Singleton3.class) {
                if (instance == null) {
                    Thread.sleep(500);
                    instance = new Singleton3();
                }
            }
        }
        return instance;
    }

}
