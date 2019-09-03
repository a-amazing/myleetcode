package com.study.designPattern.composite.transparent;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public interface Component {

    public void add(Component c);

    public void remove(Component c);

    public Component getChild(int i);

    public void operate();
}
