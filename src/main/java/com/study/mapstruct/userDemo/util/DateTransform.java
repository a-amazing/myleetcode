package com.study.mapstruct.userDemo.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author:wangyi
 * @Date:2020/4/22
 */
public class DateTransform {
    public static LocalDateTime strToDate(String str){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse("2018-01-12 17:07:05",df);
    }
}
