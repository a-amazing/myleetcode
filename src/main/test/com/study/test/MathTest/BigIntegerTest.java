package com.study.test.MathTest;

import java.math.BigInteger;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class BigIntegerTest {
    public static void main(String[] args) {
        BigInteger n = new BigInteger("999999").pow(99);
        float f = n.floatValue();
        System.out.println(f);//Infinity
    }
}
