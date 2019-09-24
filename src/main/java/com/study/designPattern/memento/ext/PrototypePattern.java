package com.study.designPattern.memento.ext;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class PrototypePattern {
    public static void main(String[] args) throws CloneNotSupportedException {
        OriginatorPrototype opt = new OriginatorPrototype();
        opt.setState("begins!");
        OriginatorPrototype memento = opt.createMemento();
        PrototypeCaretaker caretaker = new PrototypeCaretaker();
        caretaker.setMemento(memento);

        opt.setState("ends!");
        opt.restoreMemento(caretaker.getMemento());

        System.out.println(opt.getState());
    }
}
