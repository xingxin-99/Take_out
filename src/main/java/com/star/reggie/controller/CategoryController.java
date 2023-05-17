package com.star.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.star.reggie.common.R;
import com.star.reggie.common.exception.DeleteIdException;
import com.star.reggie.entity.Category;
import com.star.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        Page<Category> p = new Page(page,pageSize);
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.orderByAsc(Category::getSort);

        categoryService.page(p,lambdaQueryWrapper);
        return R.success(p);
    }

    @PostMapping
    public R<String> post(@RequestBody Category category){
        categoryService.save(category);
        return R.success("成功添加该分类菜品！");

    }
    @DeleteMapping
    public R<String> deleteByID(Long ids){

        categoryService.remove(ids);
        return R.success("删除成功");

    }
    @PutMapping
    public R<String> updateById(@RequestBody Category category){

        categoryService.updateById(category);
        return R.success("修改成功");
    }
    @GetMapping("/list")
    public R<List<Category>> getCategoryList(Category category){
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list= categoryService.list(lqw);
        return R.success(list);
    }



}
