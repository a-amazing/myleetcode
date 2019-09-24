package com.study.designPattern.memento.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class MementoPattern {
    public static void main(String[] args) {
        Originator originator = new Originator();
        originator.setState("start");
        Memento startMemento = originator.createMemento();
        Caretaker caretaker = new Caretaker();
        caretaker.setMemento(startMemento);

        originator.setState("doing");
        Memento doingMemento = originator.createMemento();

        originator.restoreMemento(caretaker.getMemento());
        System.out.println(originator.getState());
    }
}
