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
public class UserEnum {
    private Integer id;
    private String name;
    private UserTypeEnum userTypeEnum;
}
