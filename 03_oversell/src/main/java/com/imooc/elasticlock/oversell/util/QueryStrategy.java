package com.imooc.elasticlock.oversell.util;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;
import java.util.function.Consumer;

public interface QueryStrategy<T> {
    default Boolean condition() {
        return true;
    }

    default Boolean not(){
        return false;
    }

    default void setNot(Boolean not){};

    default List<QueryStrategy<T>> and(){
        return null;
    }
    default Consumer<QueryWrapper<T>> or(){
        return null;
    }

    <C extends AbstractWrapper<T,String,C>> List<Consumer<AbstractWrapper<T,String,C>>> getConditionConsumers();
}
