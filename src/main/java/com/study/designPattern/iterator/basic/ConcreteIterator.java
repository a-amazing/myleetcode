package com.study.designPattern.iterator.basic;

import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteIterator implements Iterator {
    private List list;
    private int index;

    public ConcreteIterator(List list) {
        if (list == null) {
            throw new NullPointerException();
        }
        this.list = list;
        index = -1;
    }

    @Override
    public Object first() {
        if (null == list) {
            throw new IndexOutOfBoundsException();
        } else if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Object next() {
        if (hasNext()) {
            return list.get(++index);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean hasNext() {
        if (index + 1 > list.size() - 1) {
            return false;
        }
        return true;
    }
}
