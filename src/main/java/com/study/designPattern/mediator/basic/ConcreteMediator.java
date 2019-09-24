package com.study.designPattern.mediator.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class ConcreteMediator extends Mediator {
    private List<Colleague> list = new ArrayList<>();

    @Override
    public void register(Colleague colleague) {
        if (!list.contains(colleague)) {
            list.add(colleague);
            colleague.setMediator(this);
        }
    }

    @Override
    public void relay(Colleague colleague) {
        for (Colleague ob : list) {
            if (!ob.equals(colleague)) {
                ((Colleague) ob).receive();
            }
        }
    }
}
