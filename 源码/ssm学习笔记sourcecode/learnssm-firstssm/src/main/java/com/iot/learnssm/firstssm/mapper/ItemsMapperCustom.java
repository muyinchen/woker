package com.iot.learnssm.firstssm.mapper;

import com.iot.learnssm.firstssm.po.ItemsCustom;
import com.iot.learnssm.firstssm.po.ItemsQueryVo;

import java.util.List;

public interface ItemsMapperCustom {
    //商品查询列表
    List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo) throws Exception;
}