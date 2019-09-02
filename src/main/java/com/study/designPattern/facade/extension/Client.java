package com.study.designPattern.facade.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class Client {
    public static void main(String[] args) {
        SubSystem01 obj1 = new SubSystem01();
        SubSystem02 obj2 = new SubSystem02();
        SubSystem03 obj3 = new SubSystem03();
        SubSystem04 obj4 = new SubSystem04();
        AbstractFacade facade = new Facade1(obj1,obj2,obj3);
        facade.method1();
    }
}
