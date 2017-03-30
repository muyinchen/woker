package com.iot.ssm.mapper;

import com.iot.ssm.po.Items;
import com.iot.ssm.po.ItemsExample;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.List;

public class ItemsMapperTest {

    private ApplicationContext applicationContext;

    private ItemsMapper itemsMapper;

    //在setUp这个方法得到spring容器
    @Before
    public void setUp() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
        itemsMapper = (ItemsMapper) applicationContext.getBean("itemsMapper");
    }

    //根据主键删除
    @Test
    public void testDeleteByPrimaryKey() {

    }

    //插入
    @Test
    public void testInsert() {
        //构造 items对象
        Items items = new Items();
        items.setName("手机");
        items.setPrice(999f);
        items.setCreatetime(new Date());
        itemsMapper.insert(items);
    }

    //自定义条件查询
    @Test
    public void testSelectByExample() {
        ItemsExample itemsExample = new ItemsExample();
        //通过criteria构造查询条件
        ItemsExample.Criteria criteria = itemsExample.createCriteria();
        criteria.andNameEqualTo("笔记本");
        //可能返回多条记录
        List<Items> list = itemsMapper.selectByExample(itemsExample);

        System.out.println(list);

    }

    //根据主键查询
    @Test
    public void testSelectByPrimaryKey() {
        Items items = itemsMapper.selectByPrimaryKey(1);
        System.out.println(items);
    }

    //更新数据
    @Test
    public void testUpdateByPrimaryKey() {

        //对所有字段进行更新，需要先查询出来再更新
        Items items = itemsMapper.selectByPrimaryKey(1);

        items.setName("手机");

        itemsMapper.updateByPrimaryKey(items);
        //如果传入字段不空为才更新，在批量更新中使用此方法，不需要先查询再更新
        //itemsMapper.updateByPrimaryKeySelective(record);

    }

}
