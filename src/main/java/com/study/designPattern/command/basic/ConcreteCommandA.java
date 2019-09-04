package com.study.designPattern.command.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteCommandA implements Command {
    private ReceiverA receiver;

    public ConcreteCommandA(ReceiverA receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        System.out.println("CommandA invoke execute!");
        receiver.action();
    }
}
