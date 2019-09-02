package com.study.designPattern.facade.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class Facade2 implements AbstractFacade {
    private SubSystem02 subSystem02;
    private SubSystem03 subSystem03;
    private SubSystem04 subSystem04;

    public Facade2() {
    }

    public Facade2(SubSystem02 subSystem02, SubSystem03 subSystem03, SubSystem04 subSystem04) {
        this.subSystem02 = subSystem02;
        this.subSystem03 = subSystem03;
        this.subSystem04 = subSystem04;
    }

    public void method1(){
        subSystem03.method3();
    }

    public void method2(){
        subSystem04.method4();
    }
}
