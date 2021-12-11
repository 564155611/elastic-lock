package com.imooc.elasticlock.oversell.entity;

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
 * 产品表 
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Repository
public class Product extends BaseEntity<Product> {

    private static final long serialVersionUID = 1L;

    /**
     * 商品名称
     */
    @QueryFilter(Fuzzy.class)
    private String productName;

    /**
     * 商品价格
     */
    @QueryFilter(Range.class)
    private BigDecimal price;

    /**
     * 库存数量
     */
    @QueryFilter(Range.class)
    private Integer count;

    /**
     * 商品描述
     */
    @QueryFilter(Fuzzy.class)
    private String productDesc;

}
