package com.study.lambdaDemo.classDemo;

import java.util.List;

/**
 * @author:wangyi
 * @Date:2019/11/4
 */
public class Student {
    private String name;
    private int age;
    private int stature;
    private List<SpecilityEnum> specilities;

    public Student() {
    }

    public Student(String name, int age, int stature) {
        this.name = name;
        this.age = age;
        this.stature = stature;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getStature() {
        return stature;
    }

    public List<SpecilityEnum> getSpecilities() {
        return specilities;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", stature=" + stature +
                ", specilities=" + specilities +
                '}';
    }
}
