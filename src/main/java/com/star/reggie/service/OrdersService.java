package com.star.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.star.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
