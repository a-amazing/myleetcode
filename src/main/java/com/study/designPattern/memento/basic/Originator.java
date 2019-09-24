package com.study.designPattern.memento.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Originator {
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Memento createMemento() {
        return new Memento(this.state);
    }

    public void restoreMemento(Memento memento) {
        setState(memento.getState());
    }
}
