package com.study.designPattern.mediator.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteColleagueA extends Colleague {

    @Override
    public void receive() {
        System.out.println("具体同事类A收到请求。");
    }

    @Override
    public void send() {
        System.out.println("具体同事类A发出请求。");
        mediator.relay(this); //请中介者转发
    }
}
