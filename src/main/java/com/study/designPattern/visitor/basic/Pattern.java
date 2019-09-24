package com.study.designPattern.visitor.basic;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class Pattern {


    public static void main(String[] args) {
        ObjectStructure os = new ObjectStructure();
        os.add(new ConcreteElementA());
        os.add(new ConcreteElementB());

        Visitor a = new ConcreteVisitorA();
        Visitor b = new ConcreteVisitroB();

        os.accept(a);
        os.accept(b);

    }
}
