package com.iot.learnssm.firstssm.controller;

import com.iot.learnssm.firstssm.po.ItemsCustom;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by brian on 2016/3/7.
 */
@Controller
public class JsonTest {
    //请求json串(商品信息)，输出json(商品信息)
    //@RequestBody将请求的商品信息的json串转成itemsCustom对象
    //@ResponseBody将itemsCustom转成json输出
    @RequestMapping("/requestJson")
    public
    @ResponseBody
    ItemsCustom requestJson(@RequestBody ItemsCustom itemsCustom) {

        //@ResponseBody将itemsCustom转成json输出
        return itemsCustom;
    }

    //请求key/value，输出json
    @RequestMapping("/responseJson")
    public
    @ResponseBody
    ItemsCustom responseJson(ItemsCustom itemsCustom) {

        //@ResponseBody将itemsCustom转成json输出
        return itemsCustom;
    }

}
