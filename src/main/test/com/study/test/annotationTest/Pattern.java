package com.study.test.annotationTest;

import java.lang.reflect.Method;

/**
 * @author:wangyi
 * @Date:2019/9/17
 */
public class Pattern {

    public static void main(String[] args) throws NoSuchMethodException {
        Class<Son> clazz = Son.class;
        Son son = new Son();
        son.test();
        Method method = clazz.getMethod("test");
        MyAnno annotation = method.getAnnotation(MyAnno.class);
        if(annotation != null){
            System.out.println(annotation.value());
        }else{
            System.out.println("注解为空!");
        }
        Method test2 = clazz.getMethod("test2");
        MyAnno anno2 = test2.getAnnotation(MyAnno.class);
        if(anno2 != null){
            System.out.println(anno2.value());
        }else{
            System.out.println("test2 注解为空");
        }
    }
}
