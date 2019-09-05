package com.study.designPattern.state.multiThreadDemo;

/**
 * @author:wangyi
 * @Date:2019/9/5
 */
public class Running extends ThreadState {
    public Running() {
    }

    public void suspend(ThreadContext env) {
        System.out.println("thread suspend");
        env.suspend();
    }

    public void stop(ThreadContext env){
        System.out.println("thread stop!");
        env.stop();
    }
}

