package com.star.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.reggie.common.exception.DeleteIdException;
import com.star.reggie.dto.SetmealDto;
import com.star.reggie.entity.Setmeal;
import com.star.reggie.entity.SetmealDish;
import com.star.reggie.mapper.SetmealMapper;
import com.star.reggie.service.SetmealDishService;
import com.star.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    @Override
    public void savewithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> collect = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(collect);
    }

    @Override
    public SetmealDto getWithDishById(Long id) {
        Setmeal setmeal = this.getById(id);
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        Long id = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        setmealDishService.remove(lambdaQueryWrapper);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> collect = setmealDishes.stream().map((item) -> {
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(collect);
    }

    @Override
    public void deleteById(List<Long> ids) {
        //首先查询套餐的状态，判断套餐是否可以删掉
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
        int count = this.count(lqw);
        if(count>0){
            throw new DeleteIdException("套餐正在售卖，删除失败！");
        }
        this.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }

    @Override
    public List<SetmealDto> getWithAllDishByCategoryId(Setmeal setmeal) {
        //根据分类id，查询出所有在售状态的套餐id
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmealList = this.list(lambdaQueryWrapper);
        //根据套餐id，查询出对应的菜品
        List<SetmealDto> collect = setmealList.stream().map((item) -> {
            Long setmealId = item.getId();
            LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
            lqw.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> dishList = setmealDishService.list(lqw);
            //整合上述的信息，封装到SetmealDto中
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            setmealDto.setSetmealDishes(dishList);
            return setmealDto;
        }).collect(Collectors.toList());

        //
        return collect;
    }
}
