package com.study.mapstruct.userDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author:wangyi
 * @Date:2020/4/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO3 {
    private String id;
    private String name;
    // 实体类该属性是String
    private LocalDateTime createTime;
    // 实体类该属性是LocalDateTime
    private String updateTime;
}
