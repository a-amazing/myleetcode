package com.study.designPattern.flyweight.compositeFlyweight;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class CompositeConcreteFlyweight implements Flyweight {

    private List<Flyweight> list = new ArrayList<>();

    public void add(Flyweight flyweight) {
        list.add(flyweight);
    }

    public void remove(Flyweight flyweight) {
        list.remove(flyweight);
    }

    public void operate(UnsharedConcreteFlyweight state) {

    }
}
