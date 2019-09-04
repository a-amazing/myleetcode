package com.study.designPattern.responseChain.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteHandlerB extends Handler {

    @Override
    public boolean isCompatible(String request) {
        return true;
    }

    @Override
    public void doHandle(String request){
        System.out.println("ConcreteHandlerB handle request!");
    }
}
