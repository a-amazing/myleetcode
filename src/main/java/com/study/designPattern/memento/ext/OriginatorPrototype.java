package com.study.designPattern.memento.ext;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class OriginatorPrototype implements Cloneable {
    private String state;

    public OriginatorPrototype createMemento() throws CloneNotSupportedException {
        return (OriginatorPrototype) this.clone();
    }

    public void restoreMemento(OriginatorPrototype prototype){
        this.setState(prototype.getState());
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
