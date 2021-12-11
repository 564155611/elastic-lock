package com.imooc.elasticlock.oversell.util;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Setter
public class Fuzzy<T> extends FieldQueryStrategy<T> {
    public static final String BEGINS_WITH= "B";
    public static final String ENDS_WITH = "E";
    public static final String CONTAINS = "C";
    private String value;
    private String type;

    public <C extends AbstractWrapper<T,String,C>> List<Consumer<AbstractWrapper<T,String,C>>> getConditionConsumers() {
        List<Consumer<AbstractWrapper<T,String,C>>> ret = new ArrayList<>();
        String column = getColumn();
        switch (type) {
            case BEGINS_WITH:
                ret.add(w -> {
                    w.likeRight(condition(), column, value);
                });
                break;
            case ENDS_WITH:
                ret.add(w -> w.likeLeft(condition(),column, value));
                break;
            /*case CONTAINS:
                ret.add(w -> w.like(condition(),column, value));
                break;*/
            default:
                ret.add(w -> w.like(condition(),column, value));
        }
        return ret;
    }
}
