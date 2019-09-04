package com.study.designPattern.command.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class Invoker {
    private Command command;

    public Invoker(Command command){
        this.command = command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public void call(){
        this.command.execute();
    }
}
