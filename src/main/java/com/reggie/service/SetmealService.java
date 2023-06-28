package com.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void addSetmeal(SetmealDto setmealDto);

    Page<SetmealDto> getSetmealPage(Integer page, Integer pageSize, String name);

    void deleteSetmeal(String ids);

    void setmealStatusByStatus(Integer status, String ids);

    SetmealDto querySetmealById(Long id);

    void editSetmeal(SetmealDto setmealDto);

    List<SetmealDto> setmealList(Setmeal setmeal);
}
