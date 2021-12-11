package com.imooc.elasticlock.oversell.util.annotations;

import com.imooc.elasticlock.oversell.util.Precise;
import com.imooc.elasticlock.oversell.util.FieldQueryStrategy;

public @interface QueryFilter {
    Class<? extends FieldQueryStrategy>[] value() default {Precise.class};
}
