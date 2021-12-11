package com.imooc.elasticlock.oversell.util;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public abstract class FieldQueryStrategy<T> implements QueryStrategy<T> {
    private String name;
    private String column;
    private Boolean not=false;

    @Override
    public Boolean not() {
        return not;
    }

    public String getColumn() {
        if (StringUtils.isNotBlank(column)) {
            String name = getName();
            if (StringUtils.isNotBlank(name)) {
                column = StrUtil.toUnderlineCase(name.toString()).toUpperCase();
            }
        }
        return column;
    }

}
