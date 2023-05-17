package com.star.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.star.reggie.common.R;
import com.star.reggie.entity.Employee;
import com.star.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")

public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        long ids = Thread.currentThread().getId();
        log.info("线程id:{}",ids);

        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        if(emp==null)
            return R.error("该用户不存在！");

        if(!emp.getPassword().equals(password))
            return R.error("密码错误！");

        if(emp.getStatus()==0){
            return R.error("用户被禁用！");
        }

        request.getSession().setAttribute("employee",emp.getId());
        log.info("登陆成功");

        return R.success(emp);

    };
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("移除成功");
    };

    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        long ids = Thread.currentThread().getId();
        log.info("线程id:{}",ids);
//        employeeService.save(employee);
        log.info(employee.toString());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));


        employeeService.save(employee);

        return R.success("创建成功");

    };

    @GetMapping("/page")
    public R<Page>  page(int page, int pageSize, String name){
        long ids = Thread.currentThread().getId();
        log.info("线程id:{}",ids);

        log.info("page={},pagesize={},name={}",page,pageSize,name);
        Page pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        long ids = Thread.currentThread().getId();
        log.info("线程id:{}",ids);

        log.info(employee.toString());

        employeeService.updateById(employee);
        return R.success("员工修改信息成功！");
    }
    @GetMapping("/{id}")
    public R<Employee> selectById(@PathVariable Long id){
        long ids = Thread.currentThread().getId();
        log.info("线程id:{}",ids);

        Employee employee = employeeService.getById(id);
        log.info("选择"+id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("出错！");
    }
}
