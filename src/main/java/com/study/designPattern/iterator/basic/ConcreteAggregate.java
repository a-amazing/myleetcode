package com.study.designPattern.iterator.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteAggregate implements Aggregate {
    private List list;

    public ConcreteAggregate(){
        list = new ArrayList();
    }

    @Override
    public void add(Object obj) {
        list.add(obj);
    }

    @Override
    public void remove(Object obj) {
        list.remove(obj);
    }

    @Override
    public Iterator getIterator() {
        return new ConcreteIterator(this.list);
    }
}
