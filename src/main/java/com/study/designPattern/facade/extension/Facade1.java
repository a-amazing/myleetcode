package com.study.designPattern.facade.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class Facade1 implements AbstractFacade{

    private SubSystem01 subSystem01;
    private SubSystem02 subSystem02;
    private SubSystem03 subSystem03;

    public Facade1() {
    }

    public Facade1(SubSystem01 subSystem01, SubSystem02 subSystem02, SubSystem03 subSystem03) {
        this.subSystem01 = subSystem01;
        this.subSystem02 = subSystem02;
        this.subSystem03 = subSystem03;
    }

    public void method1(){
        subSystem01.method1();
    }

    public void method2(){
        subSystem02.method2();
    }
}
