package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.Result;
import com.reggie.entity.Category;
import com.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    //新增彩票分类----或---新增套餐分类
    @PostMapping
    public Result<String> saveCategory(@RequestBody Category category) {
        log.info(category.toString());
        categoryService.save(category);
        if (category.getType() == 1) {
            return Result.success("新增分类成功");
        }
        return Result.success("新增套餐成功");
    }

    //分页查询
    @GetMapping("/page")
    public Result<Page> getCategoryPage(Integer page, Integer pageSize) {
        log.info("分类管理分页查询---page:{},pageSize:{}", page, pageSize);
        //新建分页
        Page<Category> categoryPage = new Page<>(page, pageSize);
        //新建条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //查询结果排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //查询
        categoryService.page(categoryPage, queryWrapper);
        //返回结果
        return Result.success(categoryPage);
    }

    //删除分类
    @DeleteMapping
    public Result<String> deleteCategory(Long ids) {
        categoryService.removeById(ids);
        return Result.success("删除成功");
    }

    //根据ID修改分类
    @PutMapping
    public Result<String> updateCategory(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("修改成功");
    }

    //查询菜品分类
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(Category category) {
        //构造查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //查询
        List<Category> list = categoryService.list(queryWrapper);
        //返回结果
        return Result.success(list);
    }
}
