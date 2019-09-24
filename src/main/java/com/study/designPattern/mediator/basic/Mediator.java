package com.study.designPattern.mediator.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public abstract class Mediator {

    public abstract void register(Colleague colleague);

    public abstract void relay(Colleague colleague);
}
