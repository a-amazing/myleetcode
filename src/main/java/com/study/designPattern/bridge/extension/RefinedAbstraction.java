package com.study.designPattern.bridge.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class RefinedAbstraction extends Abstraction {

    protected RefinedAbstraction(Implementor impl){
        super(impl);
    }

    @Override
    public void operate() {
        super.impl.operateImpl();
    }
}
