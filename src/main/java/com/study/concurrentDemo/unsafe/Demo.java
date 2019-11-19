package com.study.concurrentDemo.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author wangyi
 * @date 2019/11/15
 */
public class Demo {
    private int age = 0;

    public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException {
//        Unsafe unsafe = Unsafe.getUnsafe();//不允许非jdk类使用unsafe
        Class<Unsafe> clazz = Unsafe.class;
        Field field = clazz.getField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        long offset = unsafe.objectFieldOffset(Demo.class.getField("age"));

        Demo demo = new Demo();
        System.out.println(demo.age);
        unsafe.putInt(demo,offset,100);
        System.out.println(demo.age);
    }
}
