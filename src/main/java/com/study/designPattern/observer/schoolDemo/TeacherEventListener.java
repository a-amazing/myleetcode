package com.study.designPattern.observer.schoolDemo;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class TeacherEventListener implements BellEventListener {
    @Override
    public void heardBell(RingEvent event) {
        System.out.println("teacher sound the ring. ");
        if(!event.getSound()){
            System.out.println("class is over!");
        }else{
            System.out.println("class begin!");
        }

    }
}
