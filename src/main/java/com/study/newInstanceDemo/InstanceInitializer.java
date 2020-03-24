package com.study.newInstanceDemo;

/**
 * @author wangyi
 * @date 2020/03/24
 */
public class InstanceInitializer {
    private int j = getI();
    private int i = 1;

    public InstanceInitializer() {
        i = 2;
    }

    private int getI() {
        return i;
    }

    public static void main(String[] args) {
        InstanceInitializer ii = new InstanceInitializer();
        System.out.println(ii.j);
    }
}
