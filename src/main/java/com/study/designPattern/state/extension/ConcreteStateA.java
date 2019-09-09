package com.study.designPattern.state.extension;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class ConcreteStateA extends ShareState {

    @Override
    public void handle(ShareContext context) {
        System.out.println("当前状态为A,切换状态为B!");
        context.setShareState(context.getShareState("B"));
    }
}
