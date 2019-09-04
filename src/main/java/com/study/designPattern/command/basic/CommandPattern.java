package com.study.designPattern.command.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class CommandPattern {
    public static void main(String[] args)
    {
        Command cmd=new ConcreteCommandA(new ReceiverA());
        Invoker ir=new Invoker(cmd);
        System.out.println("客户访问调用者的call()方法...");
        ir.call();
    }
}
