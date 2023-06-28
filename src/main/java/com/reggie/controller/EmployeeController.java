package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.Result;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //员工登陆功能
    @PostMapping("/login")
    public Result<Employee> login(@RequestBody Employee employee, HttpServletRequest request) {
        //将页面提交的密码进行md5加密
        String password = employee.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        //将页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //如果没有查询到结果则返回登录失败
        if (emp == null) {
            return Result.error("用户名错误");
        }
        //如果查询成功则进行密码比对
        if (!emp.getPassword().equals(md5Password)) {
            return Result.error("密码错误");
        }
        //查询员工状态，是否禁用
        if (emp.getStatus() == 0) {
            return Result.error("用户已封禁");
        }
        //登录成功，将用户ID存入Session并放回登录成功
        request.getSession().setAttribute("employee", emp.getId());
        return Result.success(emp);
    }

    //员工注销功能
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        request.removeAttribute("employee");
        return Result.success("注销成功");
    }


    //新增员工
    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id

        //Long empId = (Long) request.getSession().getAttribute("employee");

        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return Result.success("新增员工成功");
    }

    //员工管理模块分页查询
    @GetMapping("/page")
    public Result<Page> pageQuery(Integer page,
                                  Integer pageSize,
                                  String name) {
        //构造分页构造器
        //page代表查询第几页
        //pageSize代表每页多少条
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行
        employeeService.page(pageInfo, queryWrapper);
        //返回结果
        return Result.success(pageInfo);
    }


    /*根据ID修改员工信息*/
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        //更改修改时间
        employee.setUpdateTime(LocalDateTime.now());
        //更改修改人
        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //修改
        employeeService.updateById(employee);
        //返回结果
        return Result.success("修改成功");
    }

    //根据ID查询员工信息
    @GetMapping("/{id}")
    public Result<Employee> getByEmployeeId(@PathVariable("id") Long id) {
        //根据ID查询员工信息
        Employee em = employeeService.getById(id);
        if (em != null) {
            return Result.success(em);
        }
        return Result.error("查询失败");
    }


}
