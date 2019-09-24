package com.study.designPattern.visitor.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteVisitorA implements Visitor {
    @Override
    public void visit(ConcreteElementA elementA) {
        System.out.println("concreteVisitorA visit ConcreteElementA");
    }

    @Override
    public void visit(ConcreteElementB elementB) {
        System.out.println("A dont't handle B");
    }
}
