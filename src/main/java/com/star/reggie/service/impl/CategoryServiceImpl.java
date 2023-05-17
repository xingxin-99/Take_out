package com.star.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.reggie.common.exception.DeleteIdException;
import com.star.reggie.entity.Category;
import com.star.reggie.entity.Dish;
import com.star.reggie.entity.Setmeal;
import com.star.reggie.mapper.CategoryMapper;
import com.star.reggie.service.CategoryService;
import com.star.reggie.service.DishService;
import com.star.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Override
    public void remove(Long id) {
        //查询该id对应的分类是否有对应的菜品或者套餐
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Dish::getCategoryId,id);
        int dishCount =  dishService.count(queryWrapper);
        if(dishCount>0){
            throw new DeleteIdException("当前分类下关联了菜品");
        }
        LambdaQueryWrapper<Setmeal> queryWrapper2 = new LambdaQueryWrapper();
        queryWrapper2.eq(Setmeal::getCategoryId,id);
        int setmealCount =  setmealService.count(queryWrapper2);
        if(setmealCount>0){
            throw new DeleteIdException("当前分类下关联了套餐");
        }

        //如果有，抛业务异常；没有则删除
        removeById(id);


    }
}
