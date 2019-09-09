package com.study.designPattern.observer.schoolDemo;

import java.util.EventObject;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class RingEvent extends EventObject {

    private static final long serialVersionUID=1L;
    private boolean sound;    //true表示上课铃声,false表示下课铃声
    public RingEvent(Object source,boolean sound)
    {
        super(source);
        this.sound=sound;
    }
    public void setSound(boolean sound)
    {
        this.sound=sound;
    }
    public boolean getSound()
    {
        return this.sound;
    }
}
