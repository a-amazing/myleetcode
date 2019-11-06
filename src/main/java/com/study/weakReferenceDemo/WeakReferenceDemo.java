package com.study.weakReferenceDemo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author:wangyi
 * @Date:2019/11/6
 */
public class WeakReferenceDemo {
    private  static  Map<Object,String> map;


    public static void doWeak(){
        map = new HashMap<>();
        WeakReference key = new WeakReference<String>("key");
        String value = "value";
        map.put(key,value);
        key = null;
        value = null;
    }

    public static void main(String[] args) {
        doWeak();
        System.out.println(map);
        System.out.println(map.size());
        System.gc();
        try {
            //休眠一下，在运行的时候加上虚拟机参数-XX:+PrintGCDetails，输出gc信息，确定gc发生了。
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(map);
        System.out.println(map.size());
    }
}
