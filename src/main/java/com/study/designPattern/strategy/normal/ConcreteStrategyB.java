package com.study.designPattern.strategy.normal;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class ConcreteStrategyB implements Strategy {
    @Override
    public void strategyMethod() {
        System.out.println("StrategyB执行了");
    }
}
