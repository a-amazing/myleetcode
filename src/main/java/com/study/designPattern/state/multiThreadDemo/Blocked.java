package com.study.designPattern.state.multiThreadDemo;

/**
 * @author:wangyi
 * @Date:2019/9/5
 */
public class Blocked extends ThreadState{

    public Blocked() {

    }

    public void resume(ThreadContext env){
        System.out.println("thread resume");
        env.resume();
    }
}
