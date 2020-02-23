package com.study.test.normalTest;

/**
 * @author wangyi
 * @date 2019/10/15
 */
public class NPETest {
    public static void main(String[] args) {
        Object obj1 = null;
        Object obj2 = null;

        System.out.println(obj1 == obj2);
    }
}
