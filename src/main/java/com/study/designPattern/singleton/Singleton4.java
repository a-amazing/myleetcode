package com.study.designPattern.singleton;

/**
 * @author:wangyi
 * @Date:2019/8/28
 */
public class Singleton4 {

    public static Singleton4 getInstance(){
        return Instance.getInstace();
    }

    private Singleton4(){}

    private static class Instance{
        private static final Singleton4 instace = new Singleton4();

        private static Singleton4 getInstace(){
            return instace;
        }
    }

}
