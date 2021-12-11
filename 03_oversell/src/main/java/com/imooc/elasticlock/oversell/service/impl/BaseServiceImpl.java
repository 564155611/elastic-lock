package com.imooc.elasticlock.oversell.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.elasticlock.oversell.mapper.OrderMapper;
import com.imooc.elasticlock.oversell.service.BaseService;
import com.imooc.elasticlock.oversell.util.*;
import com.imooc.elasticlock.oversell.util.annotations.QueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {
    private Map<String, Class<? extends FieldQueryStrategy>[]> strategies = new HashMap<>();
    private Map<String, Class<?>> propertyTypes = new HashMap<>();
    private Map<String, String> propertyColumnMap;
    private String MARK_LIKE = "%%";
    @Autowired
    ApplicationContext ctx;

    @PostConstruct
    public void init() {
        OrderMapper bean = ctx.getBean(OrderMapper.class);
        System.out.println("mapper::" + bean);
        ParameterizedType parametclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parametclass.getActualTypeArguments();
        Class<T> classT = (Class<T>) actualTypeArguments[1];
        TableInfo tableInfo = TableInfoHelper.getTableInfo(classT);
        propertyColumnMap = tableInfo.getFieldList().stream().collect(Collectors.toMap(f -> f.getProperty(), f -> f.getColumn()));
        propertyColumnMap.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        Map<String, Field> propFieldMap = TableInfoHelper.getAllFields(classT).stream()
                .collect(Collectors.toMap(Field::getName, Function.identity()));
        propertyColumnMap.forEach((prop, column) -> {
            Field field = propFieldMap.get(prop);
            Class<?> type = field.getType();
            propertyTypes.put(prop, type);
            QueryFilter annotation = field.getAnnotation(QueryFilter.class);
            if (annotation != null) {
                strategies.put(prop, annotation.value());
            }else {
                strategies.put(prop, new Class[]{Precise.class});
            }
        });
    }

    @Override
    public QueryWrapper<T> queryWrapper(Map<String, Object> params) {
        QueryWrapper<T> ret = new QueryWrapper<>();
        filterWrapper(params, ret);
        return ret;
    }

    private <C extends AbstractWrapper<T, String, C>> void filterWrapper(Map<String, Object> params, AbstractWrapper<T, String, C> ret) {
        Map<String, Class<? extends FieldQueryStrategy>[]> strategiesMap =
                new UnderlineCaseInsensitiveMap<>(strategies);
        Map<String, String> pcMap =
                new UnderlineCaseInsensitiveMap<>(propertyColumnMap);
        Map<String, Class<?>> propertyTypeMap = new UnderlineCaseInsensitiveMap<>(propertyTypes);
        params.forEach((k, v) -> {
            if (propertyColumnMap.containsKey(k)) {
                Class<? extends FieldQueryStrategy>[] classes = strategiesMap.get(k);
                String column = pcMap.get(k);
                Boolean not = detectNot(v);
                for (Class<? extends FieldQueryStrategy> qs : classes) {
                    if (Fuzzy.class.isAssignableFrom(qs)) {
                        Fuzzy<T> f = checkFuzzy(column, v, not);
                        if (f != null) {
                            f.<C>getConditionConsumers().forEach(c -> c.accept(ret));
                            break;
                        }
                    } else if (Range.class.isAssignableFrom(qs)) {
                        /*
                         * 满足范围查询的条件满足任一即可:
                         * 不为空,字符串类型,可以通过构造器构造出来
                         * 不为空,数组或集合类型且具有两个值
                         * */

                        Class<? extends Comparable> clz = (Class<? extends Comparable>) propertyTypeMap.get(k);
                        Range<T, ?> r = checkRange(column, v, not, clz);
                        if (r != null) {
                            r.<C>getConditionConsumers().forEach(c -> c.accept(ret));
                        }
                    } else {
                        Precise<T> precise = checkPrecise(column, v, not);
                        if (precise != null) {
                            precise.<C>getConditionConsumers().forEach(c -> c.accept(ret));
                        }
                    }
                }
            } else if ("$EXISTS".equalsIgnoreCase(k)) {
                Exist<T> exists = new Exist<>();
                exists.setSql(Tool.convertType(v, String.class));
                exists.<C>getConditionConsumers().forEach(c -> c.accept(ret));
            } else if ("$NOT EXISTS".equalsIgnoreCase(k)) {
                Exist<T> nexists = new Exist<>();
                nexists.setNot(true);
                nexists.setSql(Tool.convertType(v, String.class));
                nexists.<C>getConditionConsumers().forEach(c -> c.accept(ret));
            } else if ("$FIELDS".equalsIgnoreCase(k) && ret instanceof QueryWrapper) {
                if (v != null) {
                    QueryWrapper qw = ((QueryWrapper<T>) ret);
                    if (ArrayUtil.isArray(v)) {
                        qw.select(Tool.convertArrayType(v, String.class));
                    } else if (v instanceof Collection) {
                        Collection<?> col = (Collection<?>) v;
                        qw.select(Tool.convertCollectionType(col, String.class).toArray(new String[col.size()]));
                    } else if (v instanceof String) {
                        qw.select((String) v);
                    }
                }
            } else {
                System.out.println("extra key:::"+k);
                ret.apply(k, v);
            }
        });
    }

    private Precise<T> checkPrecise(String column, Object v, Boolean not) {
        Precise<T> p = new Precise<>();
        p.setColumn(column);
        p.setValue(v);
        return p;
    }

    private Boolean detectNot(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Map) {
            Object not = ((Map<?, ?>) v).get("not");
            if (not != null && not instanceof Boolean) {
                v = ((Map<?, ?>) v).get("value");
                return (Boolean) not;
            }
        }
        return false;
    }

    private <F extends Comparable<F>> Range<T, F> checkRange(String column, Object v, Boolean not, Class<F> clz) {
        if (Tool.isNullOrEmpty(v)) {
            return null;
        }
        if (v instanceof String && Comparable.class.isAssignableFrom(clz)) {
            Range<T, F> range = new Range<>((String) v, clz);
            range.setColumn(column);
            return range;
        }
        if (ArrayUtil.isArray(v)) {
            int length = Array.getLength(v);
            if (length == 2) {
                Object o1 = Array.get(v, 0);
                Object o2 = Array.get(v, 1);
                if (clz.isAssignableFrom(o1.getClass())) {
                    Range<T, F> range = new Range<>((F) o1, (F) o2, true, true);
                    range.setColumn(column);
                    return range;
                }
            }
        }
        return null;
    }

    /**
     * 满足模糊查询的条件:
     * ①不为空且两边不可以都没有"%%"
     *
     * @param k
     * @param v
     * @return
     */
    private Fuzzy<T> checkFuzzy(String k, Object v, Boolean not) {
        if (Tool.isNullOrEmpty(v) || !(v instanceof String)) {
            return null;
        }
        String str = (String) v;
        boolean e = str.startsWith(MARK_LIKE);
        boolean b = str.endsWith(MARK_LIKE);
        if (!b && !e) {
            return null;
        }
        Fuzzy<T> f = new Fuzzy<>();
        f.setColumn(k);
        f.setValue(str);
        if (b && e) {
            f.setType(Fuzzy.CONTAINS);
        } else if (b) {
            f.setType(Fuzzy.BEGINS_WITH);
        } else {
            f.setType(Fuzzy.ENDS_WITH);
        }
        return f;
    }

    @Override
    public UpdateWrapper<T> updateWrapper(Map<String, Object> setParams, Map<String, Object> filterParams) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        Map<String, String> pcMap =
                new UnderlineCaseInsensitiveMap<>(propertyColumnMap);
        setParams.forEach((k, v) -> {
            if (pcMap.containsKey(k)) {
                String column = pcMap.get(k);
                updateWrapper.set(column, v);
            } else {
                updateWrapper.setSql(k,v);
            }
        });
        filterWrapper(filterParams, updateWrapper);
        return updateWrapper;
    }

}
