package com.study.designPattern.strategy.normal;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class StrategyPattern {
    public static void main(String[] args) {
        Context context = new Context();
        context.setStrategy(new ConcreteStrategyA());
        context.strategyMethod();
    }
}
