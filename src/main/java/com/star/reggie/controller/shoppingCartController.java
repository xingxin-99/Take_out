package com.star.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.reggie.common.BaseContext;
import com.star.reggie.common.R;
import com.star.reggie.entity.ShoppingCart;
import com.star.reggie.service.ShoppingCartService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class shoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置当前用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //判断当前购物车是否存在此菜品或者套餐，如果存在，设置数量+1；如果不存在，则保存至购物车中
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        if(shoppingCart.getDishId()!=null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart s = shoppingCartService.getOne(lambdaQueryWrapper);
        if(s!=null){
            s.setNumber(s.getNumber()+1);
            shoppingCartService.updateById(s);
        }else{
            s = shoppingCart;
            s.setNumber(1);
            shoppingCartService.save(s);
        }
        s.setCreateTime(LocalDateTime.now());
        return R.success(s);
    }
    @GetMapping("list")
    public R<List<ShoppingCart>> list(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        lqw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lqw);
        return R.success(shoppingCartList);
    }

    @DeleteMapping("clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lqw);
        return R.success("删除成功！");
    }

}
