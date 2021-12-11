package com.imooc.elasticlock.oversell.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.elasticlock.oversell.util.UpdateWrapper;

import java.util.Map;

public interface BaseService<T> extends IService<T> {

    QueryWrapper<T> queryWrapper(Map<String, Object> params);

    UpdateWrapper<T> updateWrapper(Map<String, Object> setParams, Map<String, Object> filterParams);
}
