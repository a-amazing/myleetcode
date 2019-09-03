package com.study.designPattern.flyweight.compositeFlyweight;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class ConcreteFlyweight implements Flyweight {

    private String key;
    private UnsharedConcreteFlyweight outState;

    public ConcreteFlyweight(String key) {
        this.key = key;
    }

    @Override
    public void operate(UnsharedConcreteFlyweight outState) {

    }
}
