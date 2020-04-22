package com.study.test.mapstruct_test;

import com.study.mapstruct.userDemo.converter.UserCovertBasic;
import com.study.mapstruct.userDemo.domain.User;
import com.study.mapstruct.userDemo.domain.UserVO1;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @author:wangyi
 * @Date:2020/4/22
 */
public class MapStructTest {
    private UserCovertBasic covert = UserCovertBasic.INSTANCE;

    @Test
    public void testConvert(){
        User user = new User();
        user.setId(1);
        user.setName("wangyi");
        user.setUpdateTime(LocalDateTime.now());
        user.setCreateTime(LocalDateTime.now().toString());
        //System.out.println(user);
        UserVO1 userVO1 = covert.toConvertVO1(user);
        System.out.println(userVO1);
    }
}
