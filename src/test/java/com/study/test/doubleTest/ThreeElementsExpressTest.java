package com.study.test.doubleTest;

/**
 * @author:wangyi
 * @Date:2020/5/12
 */
public class ThreeElementsExpressTest {
    public static void main(String[] args) {
        boolean condition = false;
        Double value1 = 1.0D;
        Double value2 = 2.0D;
        Double value3 = null;
        Integer value4 = 4;
        // 返回类型为Double,不抛出空指针异常
        Double result1 = condition ? value1 : value3;
        System.out.println(result1);
        // 返回类型为double,会抛出空指针异常
        double result2 = condition ? value1 : value4;
        System.out.println(result2);
        // 返回类型为double,不抛出空指针异常
        Double result3 = !condition ? value1 * value2 : value3;
        System.out.println(result3);
        // 返回类型为double,会抛出空指针异常
        Double result4 = condition ? value1 * value2 : value3;
        System.out.println(result4);
    }
}
