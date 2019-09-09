package com.study.designPattern.observer.basic;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class ConcreteSubject extends Subject {
    @Override
    public void notifyObservers() {
        //只通知ObserverA
        for (Observer ob : observers) {
            if(ob instanceof ObserverA){
                System.out.println("通知观察者A!");
                ob.response();
            }else{
                System.out.println("不通知其余观察者!");
            }
        }
    }
}
