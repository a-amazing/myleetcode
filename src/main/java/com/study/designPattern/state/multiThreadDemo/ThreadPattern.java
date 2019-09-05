package com.study.designPattern.state.multiThreadDemo;

/**
 * @author:wangyi
 * @Date:2019/9/5
 */
public class ThreadPattern {
    public static void main(String[] args) {
        ThreadContext context = new ThreadContext();
        context.setState(new New());
        context.start();
    }
}
