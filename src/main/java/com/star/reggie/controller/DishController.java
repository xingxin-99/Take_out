package com.star.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.star.reggie.common.R;
import com.star.reggie.dto.DishDto;
import com.star.reggie.entity.Category;
import com.star.reggie.entity.Dish;
import com.star.reggie.entity.DishFlavor;
import com.star.reggie.service.CategoryService;
import com.star.reggie.service.DishFlavorService;
import com.star.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/dish")
@RestController
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    DishFlavorService dishFlavorService;
    @Autowired
    CategoryService categoryService;

    @GetMapping("/page")
    public R<Page<DishDto>> getPage(int page,int pageSize,String name){
        log.info("dishname-->"+name);
        Page<Dish> dishPage = new Page<Dish>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page();
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Dish::getName,name);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage,lambdaQueryWrapper);

        List<Dish> records = dishPage.getRecords();
        List<DishDto> dishDtoRecords = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            Long categoryId = item.getCategoryId();
            BeanUtils.copyProperties(item,dishDto);
            Category category = categoryService.getById(categoryId);
            if(category!=null)
                dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(dishDtoRecords);
        return R.success(dishDtoPage);
    }
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);

        return R.success("保存成功");

    }
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        Dish dish = dishService.getById(id);
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(list);
        return R.success(dishDto);
    }
    @PutMapping
    public R<String> put(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);


        return R.success("修改成功！");
    }
//    @GetMapping("/list")
//    public R<List<Dish>> getDish(Long categoryId){
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
//        lambdaQueryWrapper.eq(Dish::getCategoryId,categoryId);
//        List<Dish> list = dishService.list(lambdaQueryWrapper);
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> getWithFlavor(Dish dish){

        List<DishDto> list = dishService.getWithFlavor(dish);
        return R.success(list);
    }


}
