package com.imooc.elasticlock.oversell.entity;

import com.imooc.elasticlock.oversell.util.Range;
import com.imooc.elasticlock.oversell.util.annotations.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * <p>
 * 订单明细表 
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Repository
public class OrderItem extends BaseEntity<OrderItem> {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 购买金额
     */
    @QueryFilter(Range.class)
    private BigDecimal purchasePrice;

    /**
     * 购买数量
     */
    @QueryFilter(Range.class)
    private Integer purchaseNum;

}
