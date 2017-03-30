package com.iot.learnssm.firstssm.po;

import com.iot.learnssm.firstssm.controller.converter.validation.ValidGroup1;

import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Items {
    private Integer id;
    //校验名称在1到30字符中间
    //message是提示校验出错显示的信息
    //groups：此校验属于哪个分组，groups可以定义多个分组
    @Size(min = 1, max = 30, message = "{items.name.length.error}", groups = {ValidGroup1.class})
    private String name;

    private Float price;

    private String pic;

    //非空校验
    @NotNull(message = "{items.createtime.isNUll}")
    private Date createtime;

    private String detail;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic == null ? null : pic.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }
}