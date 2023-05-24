package com.star.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.star.reggie.dto.DishDto;
import com.star.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    public void updateWithFlavor(DishDto dishDto);

    List<DishDto> getWithFlavor(Dish dish);


}
