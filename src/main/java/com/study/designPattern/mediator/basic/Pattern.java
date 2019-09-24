package com.study.designPattern.mediator.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Pattern {
    public static void main(String[] args) {
        Mediator md=new ConcreteMediator();
        Colleague c1,c2;
        c1=new ConcreteColleagueA();
        c2=new ConcreteColleagueB();
        md.register(c1);
        md.register(c2);
        c1.send();
        System.out.println("-------------");
        c2.send();
    }
}
