package com.study.designPattern.command.compositeCommand;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class CompositeCommandPattern {
    public static void main(String[] args) {
        CompositeInvoker invoker = new CompositeInvoker();
        CompositeReceiver receiver = new CompositeReceiver();
        invoker.add((AbstractCommand) new ConcreteCommandA(receiver));
        invoker.add((AbstractCommand) new ConcreteCommandB(receiver));
        invoker.execute();
    }
}
