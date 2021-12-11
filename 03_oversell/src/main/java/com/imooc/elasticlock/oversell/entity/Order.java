package com.imooc.elasticlock.oversell.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imooc.elasticlock.oversell.util.Fuzzy;
import com.imooc.elasticlock.oversell.util.Range;
import com.imooc.elasticlock.oversell.util.annotations.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * <p>
 * 订单表 
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Repository
@TableName("`order`")
public class Order extends BaseEntity<Order> {

    private static final long serialVersionUID = 1L;


    /**
     * 订单状态 1:待支付
     */
    private Integer orderStatus;

    /**
     * 收货人姓名
     */
    @QueryFilter(Fuzzy.class)
    private String receiverName;

    /**
     * 收货人手机
     */
    private String receiverMobile;

    /**
     * 订单金额
     */
    @QueryFilter(Range.class)
    private BigDecimal orderAmount;

}
