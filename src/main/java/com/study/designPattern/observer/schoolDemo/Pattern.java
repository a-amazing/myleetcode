package com.study.designPattern.observer.schoolDemo;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class Pattern {
    public static void main(String[] args) {
        BellEventSource source = new BellEventSource();
        source.addPersonListener(new TeacherEventListener());
        source.addPersonListener(new StuEventListener());
        source.ring(true);
        source.ring(false);
    }
}
