package com.study.designPattern.visitor.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteVisitroB implements Visitor {
    @Override
    public void visit(ConcreteElementA elementA) {

        System.out.println("B dont't handle A");
    }

    @Override
    public void visit(ConcreteElementB elementB) {
        System.out.println("concreteVisitorB visit ConcreteElementB");
    }
}
