package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.Result;
import com.reggie.entity.AddressBook;
import com.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     */
    @PostMapping
    public Result<AddressBook> saveAddress(@RequestBody AddressBook addressBook) {
        Result<AddressBook> result = addressBookService.saveAddress(addressBook);
        return result;
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public Result<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        Result<AddressBook> result = addressBookService.setDefault(addressBook);
        return result;
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public Result<AddressBook> get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return Result.success(addressBook);
        }
        return Result.error("没有找到该对象");
    }

    /**
     * 根据用户id查询默认地址
     */
    @GetMapping("default")
    public Result<AddressBook> getDefault() {
        Result<AddressBook> result = addressBookService.getDefault();
        return result;
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> queryAddressBookList(AddressBook addressBook) {
        Result<List<AddressBook>> result = addressBookService.queryAddressBookList(addressBook);
        return result;
    }
}
