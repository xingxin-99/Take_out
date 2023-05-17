package com.star.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.reggie.dto.DishDto;
import com.star.reggie.entity.Dish;
import com.star.reggie.entity.DishFlavor;
import com.star.reggie.mapper.DishMapper;
import com.star.reggie.service.DishFlavorService;
import com.star.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    DishFlavorService dishFlavorService;
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        log.info(dishDto.toString());
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        updateById(dishDto);
        Long id = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        dishFlavorService.remove(lambdaQueryWrapper);
        flavors = flavors.stream().map((item)->{
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public List<DishDto> getWithFlavor(Long categoryId) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(Dish::getCategoryId,categoryId);
        List<Dish> list = this.list(lambdaQueryWrapper);
        List<DishDto> collect = list.stream().map((item) -> {
            //获取菜品id
            Long id = item.getId();
            //根据菜品id查口味表中的口味信息
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId, id);
            List<DishFlavor> list1 = dishFlavorService.list(lambdaQueryWrapper1);
            //创建DishDto实例，将菜品信息及口味信息都封装到该实例中
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            dishDto.setFlavors(list1);
            return dishDto;

            //将该实例重新返回至一个集合中
        }).collect(Collectors.toList());
        return collect;
    }
}
