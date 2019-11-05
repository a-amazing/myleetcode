package com.study.jsonDemo;

import com.alibaba.fastjson.JSONObject;

import java.util.LinkedHashMap;

/**
 * @author:wangyi
 * @Date:2019/11/5
 */
public class OrderTest {
    public static void main(String[] args) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//        obj.put("a","a");
//        obj.put("c","c");
//        obj.put("b","b");

        JSONObject obj = new JSONObject(true);
        obj.put("g","b");
        obj.put("a","a");
        obj.put("d","d");
        obj.put("c","c");
        System.out.println(obj.toJSONString());
        System.out.println(obj.toString());
    }
}
