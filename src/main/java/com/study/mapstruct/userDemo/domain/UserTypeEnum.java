package com.study.mapstruct.userDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    /**
     * java
     */
    Java("000", "Java开发工程师"),
    /**
     * db
     */
    DB("001", "数据库管理员"),
    /**
     * linux
     */
    LINUX("002", "Linux运维员");

    private String value;
    private String title;
}
