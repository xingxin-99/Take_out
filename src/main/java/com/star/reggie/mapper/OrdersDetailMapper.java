package com.star.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.reggie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersDetailMapper extends BaseMapper<OrderDetail> {
}
