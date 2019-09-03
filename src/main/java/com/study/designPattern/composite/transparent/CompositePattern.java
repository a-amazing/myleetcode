package com.study.designPattern.composite.transparent;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class CompositePattern {
    public static void main(String[] args) {
        Component component = new Composite();
        component.add(new Leaf("wangyi"));
        component.add(new Leaf("zhengkai"));
        component.operate();
    }

}
