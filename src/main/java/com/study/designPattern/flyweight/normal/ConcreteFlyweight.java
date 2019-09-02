package com.study.designPattern.flyweight.normal;

/**
 * @author wangyi
 * @date 2019/09/02
 */
public class ConcreteFlyweight implements Flyweight {
    private String key;

    public ConcreteFlyweight(String key){
        this.key = key;
    }

    @Override
    public void opreration(UnsharedConcreteFlyweight state) {
        System.out.println(state.getInfo());
    }
}
