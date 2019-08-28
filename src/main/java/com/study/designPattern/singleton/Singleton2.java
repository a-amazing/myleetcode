package com.study.designPattern.singleton;

/**
 * @author:wangyi
 * @Date:2019/8/28
 */
public class Singleton2 {
    private static Singleton2 instance = null;

    private Singleton2(){}

    public static Singleton2 getInstance(){
        if(instance == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance = new Singleton2();
        }
        return instance;
    }
}
