package com.star.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.star.reggie.entity.Category;
import com.star.reggie.entity.Employee;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);

}
