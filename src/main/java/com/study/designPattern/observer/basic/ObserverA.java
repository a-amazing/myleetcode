package com.study.designPattern.observer.basic;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class ObserverA implements Observer {
    @Override
    public void response() {
        System.out.println("观察者A关注到了变化并作出了反应!");
    }
}
