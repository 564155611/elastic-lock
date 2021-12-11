package com.imooc.elasticlock.oversell.service;

import com.imooc.elasticlock.oversell.entity.Order;

/**
 * <p>
 * 订单表  服务类
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
public interface IOrderService extends BaseService<Order> {

    //    @Transactional(rollbackFor = Exception.class)
    Integer createOrder() throws Exception;
}
