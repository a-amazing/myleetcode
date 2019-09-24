package com.study.designPattern.visitor.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ObjectStructure {

    private List<Element> list;

    public ObjectStructure(){
        list = new ArrayList<>();
    }

    public void accept(Visitor visitor){
        for (Element element : list) {
            element.accept(visitor);
        }
    }

    public void add(Element element){
        list.add(element);
    }

    public void remove(Element element){
        list.remove(element);
    }
}
