package com.study.newInstanceDemo;

/**
 * @author wangyi
 * @date 2020/03/24
 */
public class Son extends Father {
    private int si = 30;

    public Son(){
        System.out.println("si = " + si);
    }

    public static void main(String[] args) {
        Son son = new Son();
    }

    {
        System.out.println("son invoke!");
    }

    static {
        System.out.println("son static invoke");
    }
}
