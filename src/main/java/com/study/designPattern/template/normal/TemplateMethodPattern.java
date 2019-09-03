package com.study.designPattern.template.normal;

/**
 * @author:wangyi
 * @Date:2019/9/3
 */
public class TemplateMethodPattern {
    private static AbstractClass tm;

    public static void main(String[] args) {
        tm = new ConcreteClass();
        tm.templateMethod();
    }
}
