package com.study.designPattern.state.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteStateA implements State {
    @Override
    public void handle(Context context) {
        System.out.println("ConcreteStateA invoke handle");
    }
}
