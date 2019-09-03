package com.study.designPattern.flyweight.pureFlyweight;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class Client {
    public static void main(String[] args) {
        FlyweightFactory factory = new FlyweightFactory();
        Flyweight newFlyweight = factory.getFlyweight("new Flyweight");
        Flyweight oldFlyweight = factory.getFlyweight("new Flyweight");
        System.out.println(newFlyweight == oldFlyweight);
    }
}
