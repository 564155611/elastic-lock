package com.imooc.elasticlock.oversell.util;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@Setter
public class Exist<T> extends ReferenceQueryStrategy<T> {

    @Override
    public <C extends AbstractWrapper<T,String,C>> List<Consumer<AbstractWrapper<T,String,C>>> getConditionConsumers() {
        List<Consumer<AbstractWrapper<T,String,C>>> ret = new ArrayList<>();
        String sql = referenceSql();
        ret.add(not() ? w -> w.notExists(condition(), sql) : w -> w.exists(condition(), sql));
        return ret;
    }
}
