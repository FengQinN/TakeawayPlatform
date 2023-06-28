package com.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.Result;
import com.reggie.entity.Orders;
import com.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //用户提交订单，即下单
    @PostMapping("/submit")
    public Result<String> submitOrder(@RequestBody Orders orders) {
        ordersService.submitOrder(orders);
        return Result.success("下单成功");
    }

    //查询订单
    @GetMapping("/userPage")
    public Result<Page> orderPaging(Integer page, Integer pageSize) {
        Page result = ordersService.orderPaging(page, pageSize);
        return Result.success(result);
    }
}
