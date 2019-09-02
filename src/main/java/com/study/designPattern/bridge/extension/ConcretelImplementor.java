package com.study.designPattern.bridge.extension;

/**
 * @author:wangyi
 * @Date:2019/9/2
 */
public class ConcretelImplementor implements Implementor {
    @Override
    public void operateImpl() {
        System.out.println("ConcretelImplementor.operateImpl");
    }
}
