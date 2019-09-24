package com.study.designPattern.memento.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Caretaker {

    private Memento memento;

    public Memento getMemento() {
        return memento;
    }

    public void setMemento(Memento memento) {
        this.memento = memento;
    }
}
