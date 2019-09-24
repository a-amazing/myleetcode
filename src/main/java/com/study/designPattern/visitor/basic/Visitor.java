package com.study.designPattern.visitor.basic;

public interface Visitor {
    public void visit(ConcreteElementA elementA);

    public void visit(ConcreteElementB elementB);
}
