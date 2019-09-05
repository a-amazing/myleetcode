package com.study.designPattern.flyweight.normal;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangyi
 * @date 2019/09/02
 */
public class FlyweightFactory {

    private Map<String,Flyweight> flyweights = new HashMap<>();

    public Flyweight getFlyweight(String key){
        Flyweight flyweight =  flyweights.get(key);
        if (flyweight == null){
            flyweight = new ConcreteFlyweight(key);
            flyweights.put(key,flyweight);
        }
        return flyweight;
    }
}