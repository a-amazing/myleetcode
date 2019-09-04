package com.study.designPattern.strategy.strategyFactory;

import java.util.HashMap;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class StrategyFactory {
    private HashMap<String,Strategy> strategies;

    public StrategyFactory(){
        super();
        strategies = new HashMap<>();
    }

    public void put(String key,Strategy strategy){
        strategies.put(key,strategy);
    }

    public Strategy get(String key){
        return strategies.get(key);
    }

    public void strategyMethod(String key){
        Strategy strategy = get(key);
        if (strategy != null) {
            strategy.strategyMethod();
        }else{
            throw new NullPointerException();
        }
    }
}
