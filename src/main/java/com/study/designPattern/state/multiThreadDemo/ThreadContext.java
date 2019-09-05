package com.study.designPattern.state.multiThreadDemo;

/**
 * @author:wangyi
 * @Date:2019/9/5
 */
public class ThreadContext {
    private ThreadState state;

    public ThreadContext(){}

    public ThreadState getState() {
        return state;
    }

    public void setState(ThreadState state) {
        this.state = state;
    }
    public void start(){
        setState(new Runnable());
    }

    public void getCPU(){
        setState(new Running());
    }

    public void suspend(){
        setState(new Blocked());
    }

    public void stop(){
        setState(new Dead());
    }

    public void resume(){
        setState(new Running());
    }
}
