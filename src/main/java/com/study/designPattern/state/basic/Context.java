package com.study.designPattern.state.basic;

public class Context {

    private State state;

    public Context() {
        super();
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return this.state;
    }

    public void handle() {
        state.handle(this);
    }

}