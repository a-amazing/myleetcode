package com.study.lambdaDemo.classDemo;

public enum SpecilityEnum {

    SING(0, " 唱歌"), DANCE(1, "跳舞"), SWIMMING(2, "游泳"), RUNNING(3, "跑步");
    private int code;
    private String name;

    SpecilityEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SpecilityEnum{" +
                "name='" + name + '\'' +
                '}';
    }
}
