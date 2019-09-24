package com.study.designPattern.mediator.ext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class SimpleMediator {

    private static final SimpleMediator smd = new SimpleMediator();

    private List<SimpleColleague> colleagues = new ArrayList<>();

    public static SimpleMediator getMediator() {
        return smd;
    }

    public void register(SimpleColleague colleague) {
        if (!colleagues.contains(colleague)) {
            colleagues.add(colleague);
        }
    }

    public void relay(SimpleColleague colleague){
        for (SimpleColleague simpleColleague : colleagues) {
            simpleColleague.receive();
        }
    }

    private SimpleMediator() {
    }
}
