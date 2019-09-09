package com.study.designPattern.observer.schoolDemo;

import java.util.EventListener;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public interface BellEventListener extends EventListener {
    public void heardBell(RingEvent event);
}
