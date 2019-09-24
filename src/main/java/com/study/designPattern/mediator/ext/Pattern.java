package com.study.designPattern.mediator.ext;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Pattern {
    public static void main(String[] args) {
        SimpleMediator simpleMediator = SimpleMediator.getMediator();
        SimpleColleague colleagueA = new ConcreteColleagueA();
        SimpleColleague colleagueB = new ConcreteColleagueB();
        simpleMediator.register(colleagueA);
        simpleMediator.register(colleagueB);
        colleagueA.send();
        System.out.println("--------------");
        colleagueB.send();
    }
}
