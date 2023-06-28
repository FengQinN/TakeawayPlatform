package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.BaseContext;
import com.reggie.entity.*;
import com.reggie.exception.customException.CustomException;
import com.reggie.mapper.OrdersMapper;
import com.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    //用户下单
    @Override
    public void submitOrder(Orders orders) {
        //获取当前用户
        Long userId = BaseContext.getCurrentId();
        //获取当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        if (shoppingCartList.isEmpty() || shoppingCartList.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }
        //查看用户表
        User user = userService.getById(userId);
        //查询地址表
        AddressBook addressBookById = addressBookService.getById(orders.getAddressBookId());
        if (addressBookById == null) {
            throw new CustomException("地址信息有误");
        }
        //计算总金额
        //生成订单号
        long orderNumber = IdWorker.getId();
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderNumber);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        //向订单表插入数据
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderNumber));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBookById.getConsignee());
        orders.setPhone(addressBookById.getPhone());
        orders.setAddress((addressBookById.getProvinceName() == null ? "" : addressBookById.getProvinceName())
                + (addressBookById.getCityName() == null ? "" : addressBookById.getCityName())
                + (addressBookById.getDistrictName() == null ? "" : addressBookById.getDistrictName())
                + (addressBookById.getDetail() == null ? "" : addressBookById.getDetail()));
        this.save(orders);
        //向订单明细表插入数据
        orderDetailService.saveBatch(orderDetails);
        //下单完成,清空购物车
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }

    /*查询订单*/
    @Override
    public Page orderPaging(Integer page, Integer pageSize) {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //创建分页
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        //构造查询语句
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        //分页查询
        Page<Orders> ordersListPage = this.page(ordersPage, queryWrapper);
        return ordersListPage;
    }
}
