package com.imooc.elasticlock.oversell.util;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
@Setter
public class Precise<T> extends FieldQueryStrategy<T> {
    private Object value;

    @Override
    public <C extends AbstractWrapper<T,String,C>> List<Consumer<AbstractWrapper<T,String,C>>> getConditionConsumers() {
        List<Consumer<AbstractWrapper<T,String,C>>> ret = new ArrayList<>();
        String column = getColumn();
        if (value == null) {
            ret.add(not()?w -> w.isNotNull(condition(), column):w -> w.isNull(condition(), column));
        } else if (value instanceof Collection || ArrayUtil.isArray(value)) {
            ret.add(not() ? w -> w.notIn(condition(), column, value) : w -> w.in(condition(), column, value));
        } else {
            ret.add(not() ? w -> w.ne(condition(), column, value) : w -> w.eq(condition(), column, value));
        }
        return ret;
    }
}
