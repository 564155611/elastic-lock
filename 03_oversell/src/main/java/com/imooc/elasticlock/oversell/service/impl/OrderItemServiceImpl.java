package com.imooc.elasticlock.oversell.service.impl;

import com.imooc.elasticlock.oversell.entity.OrderItem;
import com.imooc.elasticlock.oversell.mapper.OrderItemMapper;
import com.imooc.elasticlock.oversell.service.IOrderItemService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表  服务实现类
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
@Service
public class OrderItemServiceImpl extends BaseServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
