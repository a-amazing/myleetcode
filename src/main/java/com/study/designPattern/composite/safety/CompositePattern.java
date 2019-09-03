package com.study.designPattern.composite.safety;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class CompositePattern {

    public static void main(String[] args) {
        Composite composite = new Composite();
        composite.add(new Leaf("wangyi"));
        composite.add(new Leaf("wanger"));

        composite.operate();
    }
}
