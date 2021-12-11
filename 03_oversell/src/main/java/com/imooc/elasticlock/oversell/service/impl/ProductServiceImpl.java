package com.imooc.elasticlock.oversell.service.impl;

import com.imooc.elasticlock.oversell.entity.Product;
import com.imooc.elasticlock.oversell.mapper.ProductMapper;
import com.imooc.elasticlock.oversell.service.IProductService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 产品表  服务实现类
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
@Service
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements IProductService {

}
