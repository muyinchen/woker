package com.iot.ssm.dao;

import com.iot.ssm.po.User;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.support.SqlSessionDaoSupport;

/**
 * Created by Brian on 2016/2/24.
 */
public class UserDaoImpl extends SqlSessionDaoSupport implements UserDao {

    @Override
    public User findUserById(int id) throws Exception {
        //继承SqlSessionDaoSupport，通过this.getSqlSession()得到sqlSessoin
        SqlSession sqlSession = this.getSqlSession();
        User user = sqlSession.selectOne("test.findUserById", id);

        return user;
    }

}
