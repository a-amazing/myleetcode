package com.study.mapstruct.userDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author:wangyi
 * @Date:2020/4/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO4 {
    // 实体类该属性名是id
    private String userId;
    // 实体类该属性名是name
    private String userName;
    private String createTime;
    private String updateTime;
}
