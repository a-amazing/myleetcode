package com.study.designPattern.responseChain.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteHandlerA extends Handler {

    @Override
    public boolean isCompatible(String request) {
        return true;
    }

    @Override
    public void doHandle(String request){
        System.out.println("ConcreteHandlerA handle request!");
    }
}
