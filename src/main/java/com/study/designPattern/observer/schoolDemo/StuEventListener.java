package com.study.designPattern.observer.schoolDemo;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class StuEventListener implements BellEventListener {
    @Override
    public void heardBell(RingEvent event) {
        System.out.println("students sound the bell rings");
        if(event.getSound()){
            System.out.println("morning,teacher!");
        }else{
            System.out.println("bye teacher");
        }
    }
}
