package com.study.designPattern.singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author:wangyi
 * @Date:2019/8/28
 */
public class SingletonTest {
    private static ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        //test Singleton1
        for (int i = 0; i < 100; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                    Singleton1 instance = Singleton1.getInstance();
//                    Singleton2 instance = Singleton2.getInstance();
//                    Singleton3 instance = null;
//                    try {
//                        instance = Singleton3.getInstance();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Singleton4 instance = Singleton4.getInstance();
                    Singleton5 instance = Singleton5.INSTANCE;
                    SingletonTest.setKey(String.valueOf(instance.hashCode()), "1");

                }
            };
            new Thread(runnable).start();
        }

        Thread.sleep(2000);
        for (String str : concurrentHashMap.keySet()) {
            System.out.println(str);
        }
    }

    public static void setKey(String key, String value) {
        concurrentHashMap.put(key, value);
    }

}
