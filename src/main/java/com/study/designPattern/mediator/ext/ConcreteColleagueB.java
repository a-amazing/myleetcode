package com.study.designPattern.mediator.ext;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteColleagueB extends SimpleColleague{

    @Override
    public void receive() {
        System.out.println("具体类B收到通知!");
    }

    @Override
    public void send() {
        System.out.println("具体类B发送通知!");
        getSimpleMediator().relay(this);
    }
}
