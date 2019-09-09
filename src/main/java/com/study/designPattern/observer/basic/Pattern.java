package com.study.designPattern.observer.basic;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class Pattern {

    public static void main(String[] args) {
        ConcreteSubject subject = new ConcreteSubject();
        subject.add(new ObserverA());
        subject.add(new ObserverB());
        subject.notifyObservers();
    }
}
