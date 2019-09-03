package com.study.designPattern.template.normal;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public abstract class AbstractClass {

    public void templateMethod(){
        specificMethod();
        abstractMethod1();
        abstractMethod2();
    }


    public void specificMethod(){
        System.out.println("AbstractClass.specificMethod()");
    }
    public abstract void abstractMethod1();
    public abstract void abstractMethod2();


}
