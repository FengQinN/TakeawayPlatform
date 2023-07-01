package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Category;
import com.reggie.entity.Setmeal;
import com.reggie.entity.SetmealDish;
import com.reggie.exception.customException.CustomException;
import com.reggie.mapper.SetmealMapper;
import com.reggie.service.CategoryService;
import com.reggie.service.SetmealDishService;
import com.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;
    //新增套餐
    @Override
    public void addSetmeal(SetmealDto setmealDto) {
        //保存到Setmeal表
        this.save(setmealDto);
        //保存到SetmealDish表
        //获取到套餐ID
        Long setmealId = setmealDto.getId();
        //获取套餐内包含的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        //写入SetmealDish表
        setmealDishService.saveBatch(setmealDishes);
    }

    //套餐分页查询
    @Override
    public Page<SetmealDto> getSetmealPage(Integer page, Integer pageSize, String name) {
        //创建分页
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //创建查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //当name字段不为空时查询
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //查询未被逻辑删除的
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询
        this.page(setmealPage, queryWrapper);
        //属性赋值
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
        //根据categoryId查询对应分类名并将查询结果赋值给setmealDtoPage的categoryName属性
        List<Setmeal> setmealList = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        //结果赋值
        setmealDtoPage.setRecords(setmealDtoList);
        //返回结果
        return setmealDtoPage;
    }

    //逻辑删除套餐
    @Override
    public void deleteSetmeal(String ids) {
        //除了这个方法，还可以使用in关键字计数，判断是否有满足status=1的数量
        List<Long> idsList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        //判断套餐是否处于售卖状态
        for (Long id : idsList) {
            Setmeal setmealById = this.getById(id);
            if (setmealById.getStatus() == 1) {
                //套餐处于售卖状态
                throw new CustomException("套餐处于售卖状态,请先停售");
            }
            //逻辑删除setmeal表中的数据
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setIsDeleted(1);
            this.updateById(setmeal);
            //逻辑删除setmeal_dish表中的数据
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId, id);
            SetmealDish setmealDish = new SetmealDish();
            setmealDish.setIsDeleted(1);
            setmealDishService.update(setmealDish, queryWrapper);
        }

    }

    //批量起售禁售
    @Override
    public void setmealStatusByStatus(Integer status, String ids) {
        List<Long> setmealIdList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        for (Long id : setmealIdList) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            this.updateById(setmeal);
        }
    }

    //查询套餐详情接口,将数据回显在修改界面
    @Override
    public SetmealDto querySetmealById(Long id) {
        //创建结果变量
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        //创建查询条件构造器
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //添加等值查询条件
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        //查询结果
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        //属性复制
        BeanUtils.copyProperties(setmeal, setmealDto);
        //属性赋值
        setmealDto.setSetmealDishes(setmealDishList);
        //返回结果
        return setmealDto;
    }

    //修改套餐数据
    @Override
    public void editSetmeal(SetmealDto setmealDto) {
        //修改Setmeal表
        this.updateById(setmealDto);
        //删除SetmealDish表数据
        Long setmealId = setmealDto.getId();
        setmealDishService.removeById(setmealId);
        //新增SetmealDish表数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    ///获取菜品分类对应的套餐
    @Override
    public List<SetmealDto> setmealList(Setmeal setmeal) {
        //从redis中获取数据
        List<SetmealDto> collect =null;
        String key = "setmeal_" + setmeal.getCategoryId() + "_" + setmeal.getStatus();
        collect = (List<SetmealDto>) redisTemplate.opsForValue().get(key);
        if (collect != null) {
            return collect;
        }
        //查询setmeal表
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId()).
                eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus()).
                eq(Setmeal::getIsDeleted, 0);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = this.list(setmealLambdaQueryWrapper);
        //根据setmeal_id查询setmeal_dish表
        collect = setmealList.stream().map((item) -> {
            Long setmealId = item.getId();
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
            //封装结果
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            setmealDto.setSetmealDishes(setmealDishList);
            return setmealDto;
        }).collect(Collectors.toList());
        //返回结果
        redisTemplate.opsForValue().set(key,collect,60, TimeUnit.MINUTES);
        return collect;
    }
}
