package com.study.designPattern.state.multiThreadDemo;

/**
 * @author:wangyi
 * @Date:2019/9/5
 */
public class New extends ThreadState {
    public New(){}

    public void start(ThreadContext env){
        System.out.println("thread start!");
        env.start();
    }
}
