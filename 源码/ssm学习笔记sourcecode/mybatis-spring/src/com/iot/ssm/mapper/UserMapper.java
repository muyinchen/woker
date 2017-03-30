package com.iot.ssm.mapper;

import com.iot.ssm.po.User;

/**
 * Created by Brian on 2016/2/24.
 */
public interface UserMapper {
    //根据id查询用户信息
    User findUserById(int id) throws Exception;

}
