package com.study.weakReferenceDemo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:wangyi
 * @Date:2019/11/5
 */
public class MapGcDemo {
    static Map<String, String> map;

    public static void main(String[] args) throws InterruptedException {
        MapGcDemo demo = new MapGcDemo();
        demo.doStrong();
        System.out.println("gc 发生前：" + map.size());
        System.out.println("开始通知GC");
        //注意，这里只是通过垃圾回收器进行垃圾回收，并不一定马上执行
        System.gc();
        Thread.sleep(1000 * 5);
        System.out.println("gc 发生后：" + map.size());

    }

    public void doStrong() {
        map = new HashMap<>();
        String key = "strongKey";
        String value = "strongValue";
        map.put(key, value);
        System.out.println(map);
        key = null;
        value = null;
        System.out.println(map);
    }
}
