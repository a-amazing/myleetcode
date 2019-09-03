package com.study.designPattern.composite.transparent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class Composite implements Component {
    private ArrayList<Component> children = new ArrayList<>();

    @Override
    public void add(Component c) {
        children.add(c);
    }

    @Override
    public void remove(Component c) {
        children.remove(c);
    }

    @Override
    public Component getChild(int i) {
        return children.get(i);
    }

    @Override
    public void operate() {
        for(Component component : children){
            component.operate();
        }
    }
}
