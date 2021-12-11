package com.imooc.elasticlock.oversell.util;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Setter
public class Range<T, F extends Comparable<F>> extends FieldQueryStrategy<T> {
    private F min;
    private F max;
    private Boolean borderMin;//是否包含左边界:min<?(false) min<=?(true)
    private Boolean borderMax;//是否包含右边界:?<max(false) ?<=max(true)

    public Range(F min, F max, Boolean borderMin, Boolean borderMax) {
        this.min = min;
        this.max = max;
        this.borderMin = borderMin;
        this.borderMax = borderMax;
    }

    public Range(String description, Class<F> classT) {
        if (StringUtils.isNotBlank(description)) {
            description = description.trim();
            if (description.startsWith("[")) {
                this.borderMin = true;
            } else if (description.startsWith("(")) {
                this.borderMin = false;
            } else {
                throw new IllegalArgumentException("未找到左边界符号:(或者[");
            }
            if (description.endsWith("]")) {
                this.borderMax = true;
            } else if (description.endsWith(")")) {
                this.borderMax = false;
            } else {
                throw new IllegalArgumentException("未找到右边界符号:)或者]");
            }
            String content = description.substring(1, description.length() - 1).trim();
            if (StringUtils.isBlank(content)) {
                throw new IllegalArgumentException("区间内容为空");
            }
            String[] split = content.split(",");
            if (split.length != 2) {
                throw new IllegalArgumentException("区间内容无法识别: <min>,<max>");
            }
            String minStr = split[0];
            String maxStr = split[1];

            if (StringUtils.isNotBlank(minStr)) {
                minStr = minStr.trim();
                if (!"-∞".equals(minStr)) {
                    this.min = Tool.convertType(minStr, classT);
                }
            }
            if (StringUtils.isNotBlank(maxStr)) {
                maxStr = maxStr.trim();
                if (!"+∞".equals(maxStr)) {
                    this.max = Tool.convertType(maxStr, classT);
                }
            }
        }
    }

    public F getMin() {
        return min;
    }

    public F getMax() {
        return max;
    }

    public Boolean isQualified(F someKey) {
        return (borderMin && (min == null || someKey.compareTo(min) >= 0) && borderMax && (max == null || someKey.compareTo(max) <= 0))
                || (borderMin && (min == null || someKey.compareTo(min) >= 0) && !borderMax && (max == null || someKey.compareTo(max) < 0))
                || (!borderMin && (min == null || someKey.compareTo(min) > 0) && borderMax && (max == null || someKey.compareTo(max) <= 0))
                || (!borderMin && (min == null || someKey.compareTo(min) > 0) && !borderMax && (max == null || someKey.compareTo(max) < 0));
    }


    public String getDescription() {
        return (Tool.isNullOrEmpty(min) || !borderMin ? "(" : "[") +
                (Tool.isNullOrEmpty(min) ? "-∞" : min) + "," +
                (Tool.isNullOrEmpty(max) ? "+∞" : max) +
                (Tool.isNullOrEmpty(max) || !borderMax ? ")" : "]");
    }

    @Override
    public <C extends AbstractWrapper<T, String, C>> List<Consumer<AbstractWrapper<T,String,C>>> getConditionConsumers() {
        List<Consumer<AbstractWrapper<T,String,C>>> ret = new ArrayList<>();
        String column = getColumn();
        boolean b1 = !Tool.isNullOrEmpty(min);
        boolean b2 = !Tool.isNullOrEmpty(max);
        if (b1 && b2 && borderMin && borderMax) {
            ret.add(w -> w.between(column, min, max));
        } else {
            if (b1) {
                Consumer<AbstractWrapper<T,String,C>> c = borderMin ?
                        w -> w.ge(column, min) :
                        w -> w.gt(column, min);
                ret.add(c);
            }
            if (b2) {
                Consumer<AbstractWrapper<T,String,C>> c = borderMax ?
                        w -> w.le(column, max) :
                        w -> w.lt(column, max);
                ret.add(c);
            }
        }
        return ret;
    }
}