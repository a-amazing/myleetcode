package com.study.designPattern.iterator.basic;

public interface Aggregate<T> {
    public void add(T obj);

    public void remove(T obj);

    public Iterator getIterator();
}
