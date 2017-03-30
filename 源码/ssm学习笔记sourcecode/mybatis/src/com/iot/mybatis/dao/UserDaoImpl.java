package com.iot.mybatis.dao;

import com.iot.mybatis.po.User;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * Created by Brian on 2016/2/24.
 */
public class UserDaoImpl implements UserDao {
    // 需要向dao实现类中注入SqlSessionFactory
    // 这里通过构造方法注入
    private SqlSessionFactory sqlSessionFactory;

    public UserDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public User findUserById(int id) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        User user = sqlSession.selectOne("test.findUserById", id);
        //释放资源
        sqlSession.close();
        return user;
    }

    @Override
    public List<User> findUserByName(String name) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        List<User> list = sqlSession.selectList("test.findUserByName", name);

        // 释放资源
        sqlSession.close();

        return list;
    }

    @Override
    public void insertUser(User user) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        //执行插入操作
        sqlSession.insert("test.insertUser", user);

        // 提交事务
        sqlSession.commit();

        // 释放资源
        sqlSession.close();
    }

    @Override
    public void deleteUser(int id) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        //执行插入操作
        sqlSession.delete("test.deleteUser", id);

        // 提交事务
        sqlSession.commit();

        // 释放资源
        sqlSession.close();
    }
}
