package com.study.designPattern.observer.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public abstract class Subject {
    protected List<Observer> observers = new ArrayList<>();

    public void add(Observer observer){
        observers.add(observer);
    }

    public void remove(Observer observer){
        observers.remove(observer);
    }

    public abstract void notifyObservers();
}

