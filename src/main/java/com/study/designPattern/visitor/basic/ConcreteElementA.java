package com.study.designPattern.visitor.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteElementA implements Element {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
