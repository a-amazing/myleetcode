package com.study.thread.threadLocalDemo;

/**
 * @author:wangyi
 * @Date:2019/11/8
 */
public class ThreadLocalDemo {
    public static void main(String[] args) {
        ThreadLocal<String> localSecret = new ThreadLocal<>();
        ThreadLocal<Integer> localNum = new ThreadLocal<>();
        localSecret.set("wangyi");
        localNum.set(18);

        System.out.println(localSecret.get());
        System.out.println(localNum.get());

        localNum.remove();
        localSecret.remove();

        System.out.println(localSecret.get());
        System.out.println(localNum.get());
    }
}
