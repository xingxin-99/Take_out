package com.star.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.reggie.common.BaseContext;
import com.star.reggie.common.exception.CustomException;
import com.star.reggie.entity.*;
import com.star.reggie.mapper.OrdersMapper;
import com.star.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    ShoppingCartService shoppingCartService;
    @Autowired
    OrdersDetailService ordersDetailService;
    @Autowired
    AddressBookService addressBookService;
    @Autowired
    UserService userService;

    @Override
    public void submit(Orders orders) {
        //提交的订单数据需要保存到订单表中，订单表中的字段都需要设置
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        //获取购物车数据
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lambdaQueryWrapper);
        //设计订单总金额初始值为0
        AtomicInteger amount = new AtomicInteger(0);
        //设置订单id
        long orderId = IdWorker.getId();
        //查询地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook==null){
            throw new CustomException("地址信息有误，不能下单！");
        }

        //计算购物车总金额，并将数据保存到订单明细表中
        List<OrderDetail> collect = shoppingCartList.stream().map((item) -> {
            //计算总金额
            BigDecimal itemAmount = item.getAmount();
            Integer itemNumber = item.getNumber();
            amount.addAndGet(itemAmount.multiply(new BigDecimal(itemNumber)).intValue());
            //将数据保存至订单明细表中
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(item, orderDetail, "id", "userId");
            orderDetail.setOrderId(orderId);
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setAddressBookId(orders.getAddressBookId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setPayMethod(orders.getPayMethod());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setRemark(orders.getRemark());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName()==null?"":addressBook.getProvinceName())
                +(addressBook.getCityName()==null?"":addressBook.getCityName())
                +(addressBook.getDistrictName()==null?"":addressBook.getDistrictName())
                +(addressBook.getDetail()==null?"":addressBook.getDetail()));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());



        //订单数据关联的订单明细数据也需要更新，把购物车中的菜品/套餐保存至订单明细中
        this.save(orders);
        ordersDetailService.saveBatch(collect);
        //将购物车中该用户的购物车数据清除

    }
}
