package com.study.designPattern.flyweight.pureFlyweight;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class FlyweightFactory {

    private Map<String,Flyweight> map = new HashMap<>();

    public Flyweight getFlyweight(String key){
        Flyweight flyweight = map.get(key);
        if(flyweight == null){
            flyweight = new ConcreteFlyweight(key);
            map.put(key,flyweight);
        }
        return flyweight;
    }

}
