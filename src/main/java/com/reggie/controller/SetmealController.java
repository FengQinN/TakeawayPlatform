package com.reggie.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.Result;
import com.reggie.dto.SetmealDto;

import com.reggie.entity.Setmeal;
import com.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/*套餐管理*/
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private RedisTemplate redisTemplate;
    //新增套餐
    @PostMapping
    public Result<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        setmealService.addSetmeal(setmealDto);
        String key = "dish_" + setmealDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return Result.success("新增套餐成功");
    }

    //套餐分页查询
    @GetMapping("/page")
    public Result<Page> getSetmealPage(Integer page, Integer pageSize, String name) {
        Page<SetmealDto> setmealDtoPage = setmealService.getSetmealPage(page, pageSize, name);
        return Result.success(setmealDtoPage);
    }

    //逻辑删除套餐
    @DeleteMapping
    public Result<String> deleteSetmeal(@RequestParam("ids") String ids) {
        setmealService.deleteSetmeal(ids);
        return Result.success("删除套餐成功");
    }

    //批量起售禁售
    @PostMapping("/status/{status}")
    public Result<String> setmealStatusByStatus(@PathVariable("status") Integer status, @RequestParam("ids") String ids) {
        setmealService.setmealStatusByStatus(status, ids);
        return Result.success("操作成功");
    }

    //查询套餐详情接口,将数据回显在修改界面
    @GetMapping("/{id}")
    public Result<SetmealDto> querySetmealById(@PathVariable("id") Long id) {
        SetmealDto setmealDto = setmealService.querySetmealById(id);
        return Result.success(setmealDto);
    }

    //修改套餐数据
    @PutMapping
    public Result<String> editSetmeal(@RequestBody SetmealDto setmealDto) {
        setmealService.editSetmeal(setmealDto);
        String key = "dish_" + setmealDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return Result.success("修改成功");
    }

    //获取菜品分类对应的套餐
    @GetMapping("/list")
    public Result<List<SetmealDto>> setmealList(Setmeal setmeal) {
        List<SetmealDto> result = setmealService.setmealList(setmeal);
        return Result.success(result);
    }
}