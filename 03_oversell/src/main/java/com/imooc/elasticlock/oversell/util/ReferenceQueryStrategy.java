package com.imooc.elasticlock.oversell.util;

import lombok.Setter;

@Setter
public abstract class ReferenceQueryStrategy<T> implements QueryStrategy<T>{
    private String sql;
    private Boolean not=false;

    public String referenceSql() {
        return sql;
    }

    public Boolean not(){
        return not;
    }
}
