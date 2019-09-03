package com.study.designPattern.flyweight.pureFlyweight;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class ConcreteFlyweight implements Flyweight {

    private String key;

    public ConcreteFlyweight(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void operate() {
        System.out.println("public class ConcreteFlyweight implements Flyweight.operate()");
    }
}
