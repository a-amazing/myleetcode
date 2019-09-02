package com.study.designPattern.bridge.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class Client {

    public static void main(String[] args) {
        Adaptee adaptee = new Adaptee();
        Implementor impl = new ObjectAdapter(adaptee);
        RefinedAbstraction ra = new RefinedAbstraction(impl);
        ra.operate();
    }
}
