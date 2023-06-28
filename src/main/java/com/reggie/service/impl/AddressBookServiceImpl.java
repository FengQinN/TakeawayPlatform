package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.BaseContext;
import com.reggie.common.Result;
import com.reggie.entity.AddressBook;
import com.reggie.mapper.AddressBookMapper;
import com.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    /**
     * 新增地址
     */
    @Override
    public Result<AddressBook> saveAddress(AddressBook addressBook) {
        //设置新增地址所属的用户
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        //保存新增地址信息
        this.save(addressBook);
        //返回结果
        return Result.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @Override
    public Result<AddressBook> setDefault(AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        //构造修改条件
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        //根据用户id等值查询
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        //根据用户id将所有的isDefault设置为0
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        //执行修改sql
        this.update(wrapper);
        //将本次修改地址的isDefault设置为1
        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        //执行修改sql
        this.updateById(addressBook);
        //返回结果
        return Result.success(addressBook);
    }

    /**
     * 根据用户id查询默认地址
     */
    @Override
    public Result<AddressBook> getDefault() {
        //构造查询语句
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //根据用户id等值查询
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        //根据isDefault等值查询
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        //SQL:select * from address_book where user_id = ? and is_default = 1
        //执行sql并返回查询实体
        AddressBook addressBook = this.getOne(queryWrapper);
        if (null == addressBook) {
            return Result.error("没有找到该对象");
        }
        return Result.success(addressBook);
    }

    @Override
    public Result<List<AddressBook>> queryAddressBookList(AddressBook addressBook) {
        //获取当前用户id
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //等值查询，当用户id不为空时，根据用户id等值查询
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        //根据更新时间降序排序
        queryWrapper.orderByDesc(AddressBook::getIsDefault).orderByDesc(AddressBook::getUpdateTime);
        //SQL:select * from address_book where user_id = ? order by update_time desc
        //返回结果集
        return Result.success(this.list(queryWrapper));
    }
}

