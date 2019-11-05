package com.study.jsonDemo;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author:wangyi
 * @Date:2019/11/5
 */
public class Entity {
    @JSONField
    private String name;
    private String address;
}
