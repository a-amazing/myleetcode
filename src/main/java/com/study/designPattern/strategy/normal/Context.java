package com.study.designPattern.strategy.normal;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class Context {

    private Strategy strategy;


    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void strategyMethod(){
        this.strategy.strategyMethod();
    }
}
