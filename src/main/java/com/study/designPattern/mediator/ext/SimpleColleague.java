package com.study.designPattern.mediator.ext;

public abstract class SimpleColleague {
    protected SimpleMediator simpleMediator;

    public SimpleMediator getSimpleMediator() {
        return simpleMediator;
    }

    public void setSimpleMediator(SimpleMediator simpleMediator) {
        this.simpleMediator = simpleMediator;
    }

    public abstract void receive();

    public abstract void send();
}
