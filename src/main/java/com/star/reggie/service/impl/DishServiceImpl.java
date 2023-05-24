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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    DishFlavorService dishFlavorService;
    @Autowired
    RedisTemplate redisTemplate;
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

        //精确清理
        String key = "Dish_" + dishDto.getCategoryId() +"_" +  dishDto.getStatus();
        redisTemplate.delete(key);

    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品信息
        updateById(dishDto);
        //获取修改的菜品id
        Long id = dishDto.getId();
        //获取修改菜品id对应的口味表，并将它从口味信息中移除
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        dishFlavorService.remove(lambdaQueryWrapper);
        //获取更改后的口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //为更改后的口味设置菜品id
        flavors = flavors.stream().map((item)->{
//            item.setId(null);
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());
        //将更改后的口味保存至口味标准
        dishFlavorService.saveBatch(flavors);

        //删除所有和dish相关的缓存
//        Set dish = redisTemplate.keys("Dish_");
//        redisTemplate.delete(dish);

        //精确清理
        String key = "Dish_" + dishDto.getCategoryId() +"_" +  dishDto.getStatus();
        redisTemplate.delete(key);
    }

    @Override
    public List<DishDto> getWithFlavor(Dish dish) {
        //动态构造key
        String key = "Dish_" + dish.getCategoryId() +"_" +  dish.getStatus();
        //先从redis中查询，如果存在，则直接返回
        List<DishDto> collect =(List<DishDto>) redisTemplate.opsForValue().get(key);
        if(collect!=null){
            return collect;
        }

        //不存在，则查询数据库
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        List<Dish> list = this.list(lambdaQueryWrapper);
        collect = list.stream().map((item) -> {
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
        //将查询的结果缓存到redis中
        redisTemplate.opsForValue().set(key,collect,60, TimeUnit.MINUTES);
        return collect;
    }
}
