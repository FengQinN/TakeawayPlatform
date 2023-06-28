package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.Result;
import com.reggie.entity.User;
import com.reggie.mapper.UserMapper;
import com.reggie.service.UserService;
import com.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {



    //生成移动端手机验证码
    @Override
    public Result<String> sendMsg(User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        log.info("/user/sendMsg接收到请求手机号:{}", phone);
        //生成验证码
        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            //调用阿里云提供的短信服务
            log.info("发送短信到手机号:{},---------验证码:{}", phone, code);
            //SMSUtils.sendMessage("阿里云短信测试", "SMS_154950909", phone, code);
            //保存验证码,暂时保存在Session中，后期替换为Redis
            session.setAttribute(phone, code);
            return Result.success("发送手机短信验证码成功");
        }
        return Result.error("发送手机短信验证码失败");
    }

    //用户登录
    @Override
    public Result<User> userLogin(Map userMap, HttpSession session) {
        //获取手机号和验证码
        String phone = userMap.get("phone").toString();
        String code = userMap.get("code").toString();
        //从Session中获取验证码
        String sessionCode = session.getAttribute(phone).toString();
        //验证码比对
        if (sessionCode != null && code.equals(sessionCode)) {
            //当前手机号是否注册,若未注册，则进行注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User queryUser = this.getOne(queryWrapper);
            if (queryUser != null){
                if (queryUser.getStatus() == 0){
                    //账号封禁
                    return Result.error("账号已封禁");
                }
                //手机号存在，登录
                //将用户ID存入session
                session.setAttribute("user",queryUser.getId());
                return Result.success(queryUser);
            }
            //注册
            User user = new User();
            user.setPhone(phone);
            this.save(user);
            return Result.success(user);
        }
        return Result.error("验证码错误");
    }


}
