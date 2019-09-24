package com.study.designPattern.mediator.ext;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteColleagueA extends SimpleColleague{

    @Override
    public void receive() {
        System.out.println("具体类A收到通知!");
    }

    @Override
    public void send() {
        System.out.println("具体类A发送通知!");
        getSimpleMediator().relay(this);
    }
}
