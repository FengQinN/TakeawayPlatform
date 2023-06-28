package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.Result;
import com.reggie.entity.ShoppingCart;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /*添加菜品或套餐购物车*/
    @PostMapping("/add")
    public Result<ShoppingCart> addShoppingCart(@RequestBody ShoppingCart shoppingCart) {
        //设置购物车用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询当前添加的菜品是否在购物车内
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (shoppingCart.getDishId() != null) {
            //当前提交的数据为菜品数据
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //添加的是套餐数据
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        if (shoppingCartOne != null) {
            // 如果在表内则将数据库内的菜品或套餐数量加1
            shoppingCartOne.setNumber((shoppingCartOne.getNumber() + 1));
            shoppingCartService.updateById(shoppingCartOne);
            //返回结果
            return Result.success(shoppingCartOne);
        }
        // 如果不在表内则将数据库内的菜品或套餐数量设置为1
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartService.save(shoppingCart);
        shoppingCartOne = shoppingCart;
        //返回结果
        return Result.success(shoppingCartOne);
    }


    //获取购物车列表信息
    @GetMapping("/list")
    public Result<List<ShoppingCart>> cartList() {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //条件构造
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件构造
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        //排序
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        //查询结果
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        //返回结果
        return Result.success(list);
    }

    //清空购物车
    @DeleteMapping("/clean")
    public Result<String> clearCart() {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //根据用户ID删除购物车所有信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //等值
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        //删除
        shoppingCartService.remove(queryWrapper);
        return Result.success("清空购物车成功");
    }

    //减少购物车中的菜品或套餐
    @PostMapping("/sub")
    public Result<ShoppingCart> updateCart(@RequestBody ShoppingCart shoppingCart) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        log.info("shoppingCart:{}", shoppingCart);
        //userID赋值
        shoppingCart.setUserId(userId);
        //删除或修改菜品或套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (shoppingCart.getDishId() != null) {
            //修改菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
            ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
            if (shoppingCartOne.getNumber() == 1) {
                shoppingCartOne.setNumber(0);
                shoppingCartService.updateById(shoppingCartOne);
            } else {
                shoppingCartOne.setNumber(shoppingCartOne.getNumber() - 1);
                shoppingCartService.updateById(shoppingCartOne);
            }
            shoppingCart = shoppingCartOne;
        } else {
            //修改套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
            ShoppingCart shoppingCartSetmealOne = shoppingCartService.getOne(queryWrapper);
            if (shoppingCartSetmealOne.getNumber() == 1) {
                shoppingCartSetmealOne.setNumber(0);
                shoppingCartService.updateById(shoppingCartSetmealOne);
            } else {
                shoppingCartSetmealOne.setNumber(shoppingCartSetmealOne.getNumber() - 1);
                shoppingCartService.updateById(shoppingCartSetmealOne);
            }
            shoppingCart = shoppingCartSetmealOne;
        }
        return Result.success(shoppingCart);
    }



}
