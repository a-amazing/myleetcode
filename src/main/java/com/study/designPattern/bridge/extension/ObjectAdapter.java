package com.study.designPattern.bridge.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class ObjectAdapter extends ConcretelImplementor {

    private Adaptee adaptee;

    public ObjectAdapter(Adaptee adaptee){
        this.adaptee = adaptee;
    }

    @Override
    public void operateImpl() {
        adaptee.specificRequest();
    }
}
