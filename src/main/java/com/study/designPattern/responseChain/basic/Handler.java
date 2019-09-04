package com.study.designPattern.responseChain.basic;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public abstract class Handler {
    private Handler next;

    public Handler getNext() {
        return next;
    }

    public void setNext(Handler next) {
        this.next = next;
    }

    public void HandleRequest(String request) {
        if (isCompatible(request)) {
            doHandle(request);
        }
        if (getNext() != null) {
            getNext().HandleRequest(request);
        }
    }

    public abstract boolean isCompatible(String request);

    public abstract void doHandle(String request);
}
