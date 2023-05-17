package com.star.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.reggie.entity.OrderDetail;
import com.star.reggie.mapper.OrdersDetailMapper;
import com.star.reggie.service.OrdersDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrdersDetailMapper, OrderDetail> implements OrdersDetailService {
}
