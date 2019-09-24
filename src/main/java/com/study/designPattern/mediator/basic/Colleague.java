package com.study.designPattern.mediator.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public abstract class Colleague {
    protected Mediator mediator;

    public Mediator getMediator() {
        return mediator;
    }

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }

    public abstract void receive();

    public abstract void send();
}
