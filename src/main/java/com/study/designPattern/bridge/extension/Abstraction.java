package com.study.designPattern.bridge.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public abstract class Abstraction {
    protected Implementor impl;

    protected Abstraction(Implementor impl){
        super();
        this.impl = impl;
    }

    public abstract void operate();
}
