package com.star.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.star.reggie.common.R;
import com.star.reggie.entity.User;
import com.star.reggie.service.UserService;
import com.star.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;
    @PostMapping("/sendMsg")
    public R<String> post(@RequestBody  User user, HttpSession httpSession){
        String phone = user.getPhone();
        log.info(user.toString());
        if(phone!=null){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info(code);
            //session中缓存验证码
//            httpSession.setAttribute(phone,code);
            //redis缓存验证码
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("验证码发送成功");
        }
        return R.error("验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession httpSession){
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

//        Object code1 = httpSession.getAttribute(phone);

        Object code1 = redisTemplate.opsForValue().get(phone);
        if(code1==null)
            return R.error("请重新发送验证码！");
        if(!code1.equals(code))
            return R.error("验证码输入错误！");

        if(code1!=null && code1.equals(code)){
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if(user==null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            httpSession.setAttribute("user",user.getId());
            redisTemplate.delete(phone);

            return R.success(user);
        }





        return R.error("登陆失败");
    }
}
