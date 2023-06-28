package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    //根据ID删除分类
    void removeById(Long id);
}
