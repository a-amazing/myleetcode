package com.study.designPattern.command.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteCommandB implements Command {
    private ReceiverB receiver;

    public ConcreteCommandB(ReceiverB receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        System.out.println("CommandB invoke execute!");
        receiver.action();
    }
}
