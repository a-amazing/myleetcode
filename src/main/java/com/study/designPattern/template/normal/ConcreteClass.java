package com.study.designPattern.template.normal;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class ConcreteClass extends AbstractClass {
    @Override
    public void abstractMethod1() {
        System.out.println("ConcreteClass实现的abstractMethod1");
    }

    @Override
    public void abstractMethod2() {
        System.out.println("ConcreteClass实现的abstractMethod2");
    }
}
