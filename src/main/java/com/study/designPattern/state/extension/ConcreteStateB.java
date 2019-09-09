package com.study.designPattern.state.extension;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class ConcreteStateB extends ShareState {

    @Override
    public void handle(ShareContext context) {
        System.out.println("当前状态为B,切换状态为A!");

        context.setShareState(context.getShareState("A"));
    }
}
