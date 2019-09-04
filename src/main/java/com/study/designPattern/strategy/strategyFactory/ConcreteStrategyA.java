package com.study.designPattern.strategy.strategyFactory;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteStrategyA implements Strategy {
    @Override
    public void strategyMethod() {
        System.out.println("StrategyA invoked!");
    }
}
