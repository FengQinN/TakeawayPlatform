package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.Result;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
@Transactional
public class DishController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    //分页查询菜品信息
    @GetMapping("/page")
    public Result<Page> getDishPage(Integer page, Integer pageSize, String name) {
        //创建分页
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //创建查询语句
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.eq(Dish::getIsDeleted, 0);
        //排序处理
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查询
        dishService.page(dishPage, queryWrapper);
        //对象属性拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        //获取数据
        List<Dish> dishRecords = dishPage.getRecords();
        List<DishDto> dishDtoRecords = null;
        //数据赋值收集
        dishDtoRecords = dishRecords.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            //根据ID查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());
        //赋值给page
        dishDtoPage.setRecords(dishDtoRecords);
        //返回结果
        return Result.success(dishDtoPage);
    }

    //删除菜品
    @DeleteMapping
    public Result<String> deleteDish(@RequestParam("ids") String ids) {
        List<Long> dishIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        for (Long dishId : dishIds) {
            Dish dish = new Dish();
            dish.setId(dishId);
            dish.setIsDeleted(1);
            dishService.updateById(dish);
        }
        return Result.success("删除成功");
    }

    //起售停售---批量起售停售菜品
    //后续使用缓存机制处理
    @PostMapping("/status/{status}")
    public Result<String> dishStatusByStatus(@PathVariable("status") Integer status, @RequestParam("ids") String ids) {
        List<Long> dishIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        for (Long dishId : dishIds) {
            Dish dish = new Dish();
            dish.setId(dishId);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return Result.success("操作成功");
    }

    //新增菜品,设计菜品表和菜品口味表
    @PostMapping
    public Result<String> addDish(@RequestBody DishDto dishDto) {
        dishService.addDish(dishDto);
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return Result.success("新增成功");
    }

    //修改菜品-菜品回显
    @GetMapping("/{id}")
    public Result<DishDto> getDishFlavorById(@PathVariable("id") Long id) {
        DishDto dishDto = dishService.getDishFlavorById(id);
        return Result.success(dishDto);
    }

    //修改菜品
    @PutMapping
    public Result<String> updateDish(@RequestBody DishDto dishDto) {
        dishService.updateDishWithFlavor(dishDto);
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return Result.success("修改成功");
    }

    //根据ID查询菜品信息
    @GetMapping("/list")
    public Result<List<DishDto>> queryDishList(Dish dish) {
        //首先在Redis中获取
        List<DishDto> collect = null;
        String key ="dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        collect = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (collect != null){
            return Result.success(collect);
        }
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加等值查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()).
                eq(dish.getStatus() != null, Dish::getStatus, dish.getStatus()).
                eq(Dish::getIsDeleted, 0);
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查询
        List<Dish> dishList = dishService.list(queryWrapper);
        collect = dishList.stream().map((item) -> {
            //获取dishID
            Long dishId = item.getId();
            //构造查询条件
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //添加等值查询
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId).eq(DishFlavor::getIsDeleted, 0);
            //查询
            List<DishFlavor> dishFlavorsList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            //封装结果
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            dishDto.setFlavors(dishFlavorsList);
            return dishDto;
        }).collect(Collectors.toList());
        //返回结果
        redisTemplate.opsForValue().set(key,collect,60, TimeUnit.MINUTES);
        return Result.success(collect);
    }
}
