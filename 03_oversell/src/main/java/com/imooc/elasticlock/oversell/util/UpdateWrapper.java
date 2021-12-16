package com.imooc.elasticlock.oversell.util;

public class UpdateWrapper<T> extends com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<T> {
    public UpdateWrapper<T> setSql(String sql,Object ...params) {
        return setSql(true, sql, params);
    }

    public UpdateWrapper<T> setSql(boolean condition, String sql,Object ...params) {
        super.setSql(formatSql(sql, params));
        return this;
    }
}
