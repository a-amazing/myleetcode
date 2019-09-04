package com.study.designPattern.responseChain.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ChainOfResponsibilityPattern {
    public static void main(String[] args) {
        ConcreteHandlerA handlerA = new ConcreteHandlerA();
        ConcreteHandlerB handlerB = new ConcreteHandlerB();
        handlerA.setNext(handlerB);
        handlerA.HandleRequest("wangyi");
    }
}
