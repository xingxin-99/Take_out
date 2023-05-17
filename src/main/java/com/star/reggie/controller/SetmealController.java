package com.star.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.star.reggie.common.R;
import com.star.reggie.dto.DishDto;
import com.star.reggie.dto.SetmealDto;
import com.star.reggie.entity.Category;
import com.star.reggie.entity.Setmeal;
import com.star.reggie.service.CategoryService;
import com.star.reggie.service.SetmealDishService;
import com.star.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    SetmealService setmealService;
    @Autowired
    CategoryService categoryService;


    @PostMapping
    public R<String> savewithDish(@RequestBody SetmealDto setmealDto){
        setmealService.savewithDish(setmealDto);
        return R.success("保存成功！");
    }
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        log.info("setmealName-->"+name);
        Page<Setmeal> pageInfo = new Page(page,pageSize);
        Page<SetmealDto> pageDtoInfo = new Page();
        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Setmeal::getName,name);
        setmealService.page(pageInfo,lambdaQueryWrapper);
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> collect = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = setmealDto.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        pageDtoInfo.setRecords(collect);
        return R.success(pageDtoInfo);
    }
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmeal(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getWithDishById(id);
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> putSetmeal(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("保存成功！");
    }
    @DeleteMapping
    public R<String> deleteByIds(@RequestParam List<Long> ids){
        setmealService.deleteById(ids);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        //根据分类id，查出该分类下所有在售状态的套餐
        List<SetmealDto> list = setmealService.getWithAllDishByCategoryId(setmeal);
        return R.success(list);
    }
}
