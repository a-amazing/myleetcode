package com.study.designPattern.state.extension;

/**
 * @author:wangyi
 * @Date:2019/9/9
 */
public class Pattern {
    public static void main(String[] args) {
        ShareContext shareContext = new ShareContext();
        shareContext.handle();
        shareContext.handle();
        shareContext.handle();
        shareContext.handle();
    }
}
