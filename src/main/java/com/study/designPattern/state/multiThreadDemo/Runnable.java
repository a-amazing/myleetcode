package com.study.designPattern.state.multiThreadDemo;

/**
 * @author:wangyi
 * @Date:2019/9/5
 */
public class Runnable extends ThreadState {

    public Runnable() {
    }

    public void getCPU(ThreadContext env){
        System.out.println("thread getCPU time!");
        env.getCPU();
    }

}
