package com.study.mapstruct.userDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author:wangyi
 * @Date:2020/4/22
 */
// 被映射类VO1:比实体类少一个字段
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO2 {
    private Integer id;
    private String name;
    private String createTime;
}
