package com.study.designPattern.composite.transparent;

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
    public void add(Component c) {

    }

    @Override
    public void remove(Component c) {

    }

    @Override
    public Component getChild(int i) {
        return null;
    }

    @Override
    public void operate() {
        System.out.println(this.name + "'s leaf.operate");
    }
}
