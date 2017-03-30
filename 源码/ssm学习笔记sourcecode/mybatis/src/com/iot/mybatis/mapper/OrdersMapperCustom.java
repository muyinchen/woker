package com.iot.mybatis.mapper;

import com.iot.mybatis.po.Orders;
import com.iot.mybatis.po.OrdersCustom;
import com.iot.mybatis.po.User;

import java.util.List;

/**
 * Created by Brian on 2016/2/25.
 */
public interface OrdersMapperCustom {
    //查询订单关联查询用户信息
    public List<OrdersCustom> findOrdersUser() throws Exception;

    //查询订单关联查询用户使用resultMap
    public List<Orders> findOrdersUserResultMap() throws Exception;

    //查询订单(关联用户)及订单明细
    public List<Orders> findOrdersAndOrderDetailResultMap() throws Exception;

    //查询用户购买商品信息
    public List<User> findUserAndItemsResultMap() throws Exception;

    //查询订单关联查询用户，用户信息是延迟加载
    public List<Orders> findOrdersUserLazyLoading() throws Exception;

}
