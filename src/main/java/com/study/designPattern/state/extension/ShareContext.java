package com.study.designPattern.state.extension;

import java.util.HashMap;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class ShareContext {
    private ShareState shareState;
    private HashMap<String,ShareState> stateSet = new HashMap<>();

    public ShareContext(){
        shareState = new ConcreteStateA();
        stateSet.put("A",shareState);
        shareState = new ConcreteStateB();
        stateSet.put("B",shareState);
        shareState = getShareState("A");
    }

    public ShareState getShareState(String key){
        this.shareState =  stateSet.get(key);
        return this.shareState;
    }

    public void setShareState(ShareState shareState){
        this.shareState = shareState;
    }

    public void handle(){
        shareState.handle(this);
    }

}
