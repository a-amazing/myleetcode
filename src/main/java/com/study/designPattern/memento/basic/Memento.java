package com.study.designPattern.memento.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Memento {
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Memento(String state) {
        this.state = state;
    }
}
