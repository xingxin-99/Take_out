package com.star.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
