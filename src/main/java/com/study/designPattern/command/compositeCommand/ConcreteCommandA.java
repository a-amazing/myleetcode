package com.study.designPattern.command.compositeCommand;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteCommandA implements AbstractCommand {
    private CompositeReceiver receiver;

    public ConcreteCommandA(CompositeReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.actionA();
    }
}
