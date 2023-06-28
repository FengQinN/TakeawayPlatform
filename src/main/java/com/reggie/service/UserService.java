package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.common.Result;
import com.reggie.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface UserService extends IService<User> {
    Result<User> userLogin(Map userMap, HttpSession session);

    Result<String> sendMsg(User user, HttpSession session);
}
