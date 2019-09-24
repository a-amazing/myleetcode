package com.study.designPattern.iterator.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Pattern {
    public static void main(String[] args) {
        ConcreteAggregate aggregate = new ConcreteAggregate();
        aggregate.add(1);
        aggregate.add(2);
        aggregate.add(3);

        Iterator iterator = aggregate.getIterator();
        System.out.println(iterator.first());
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
