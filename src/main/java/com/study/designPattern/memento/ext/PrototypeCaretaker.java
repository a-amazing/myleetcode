package com.study.designPattern.memento.ext;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class PrototypeCaretaker {

    private OriginatorPrototype opt;

    public void setMemento(OriginatorPrototype prototype){
        this.opt = prototype;
    }

    public OriginatorPrototype getMemento(){
        return this.opt;
    }

}
