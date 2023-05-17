package com.star.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.star.reggie.dto.SetmealDto;
import com.star.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void savewithDish(SetmealDto setmealDto);

    public SetmealDto getWithDishById(Long id);

    void updateWithDish(SetmealDto setmealDto);

    void deleteById(List<Long> ids);

    List<SetmealDto> getWithAllDishByCategoryId(Setmeal setmeal);
}
