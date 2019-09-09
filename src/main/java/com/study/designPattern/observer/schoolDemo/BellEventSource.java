package com.study.designPattern.observer.schoolDemo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class BellEventSource {
    protected List<BellEventListener> listeners;

    public BellEventSource() {
        this.listeners = new ArrayList<>();
    }

    public void addPersonListener(BellEventListener listener){
        listeners.add(listener);
    }

    public void ring(boolean sound){
        RingEvent ringEvent = new RingEvent(this,sound);
        notifies(ringEvent);
    }

    protected void notifies(RingEvent e){
        for (BellEventListener listener : listeners) {
            listener.heardBell(e);
        }
    }
}
