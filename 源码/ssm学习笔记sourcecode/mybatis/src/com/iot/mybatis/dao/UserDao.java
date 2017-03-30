package com.iot.mybatis.dao;

import com.iot.mybatis.po.User;

import java.util.List;

/**
 * Created by Brian on 2016/2/24.
 */
public interface UserDao {
    //根据id查询用户信息
    public User findUserById(int id) throws Exception;

    //根据用户名列查询用户列表
    public List<User> findUserByName(String name) throws Exception;

    //添加用户信息
    public void insertUser(User user) throws Exception;

    //删除用户信息
    public void deleteUser(int id) throws Exception;
}
