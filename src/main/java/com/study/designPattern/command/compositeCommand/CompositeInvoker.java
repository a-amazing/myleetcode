package com.study.designPattern.command.compositeCommand;

import java.util.ArrayList;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class CompositeInvoker implements AbstractCommand  {
    private ArrayList<AbstractCommand> children = new ArrayList<>();

    public void add(AbstractCommand command){
        children.add(command);
    }

    public void remove(AbstractCommand command){
        children.remove(command);
    }

    @Override
    public void execute() {
        for (AbstractCommand child : children) {
            child.execute();
        }
    }
}
