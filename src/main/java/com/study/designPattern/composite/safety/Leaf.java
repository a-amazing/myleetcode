package com.study.designPattern.composite.safety;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class Leaf implements Component {
    private String name;

    public Leaf(String name) {
        this.name = name;
    }

    @Override
    public void operate() {
        System.out.println(this.name + "'s leaf.operate()");
    }
}
