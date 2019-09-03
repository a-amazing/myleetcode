package com.study.designPattern.composite.safety;

import java.util.ArrayList;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class Composite implements Component {
    private ArrayList<Component> children = new ArrayList<>();

    public void add(Component component){
        children.add(component);
    }

    public void remove(Component c){
        children.remove(c);
    }

    public Component getChild(int i){
        if(i >= 0 && i < children.size()) {
            return children.get(i);
        }else{
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void operate() {
        for (Component child : children) {
            child.operate();
        }
    }
}
