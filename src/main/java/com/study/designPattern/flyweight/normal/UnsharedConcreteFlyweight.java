package com.study.designPattern.flyweight.normal;

/**
 * @author wangyi
 * @date 2019/09/02
 */
public class UnsharedConcreteFlyweight {
    private String info;

    public UnsharedConcreteFlyweight(String info) {
        this.info = info;
    }

    public void setInfo(String info){
        this.info = info;
    }

    public String getInfo(){
        return this.info;
    }
}
