package com.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.common.Result;
import com.reggie.entity.Orders;

import java.util.List;

public interface OrdersService extends IService<Orders> {
    void submitOrder(Orders orders);

    Page orderPaging(Integer page, Integer pageSize);
}
