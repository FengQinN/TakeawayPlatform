package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.common.Result;
import com.reggie.entity.AddressBook;

import java.util.List;


public interface AddressBookService extends IService<AddressBook> {
    Result<AddressBook> saveAddress(AddressBook addressBook);

    Result<AddressBook> setDefault(AddressBook addressBook);

    Result<AddressBook> getDefault();

    Result<List<AddressBook>> queryAddressBookList(AddressBook addressBook);
}
