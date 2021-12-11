package com.imooc.elasticlock.oversell.util;


import cn.hutool.core.map.CustomKeyMap;
import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 驼峰Key风格的Map<br>
 * 对KEY转换为驼峰，get("int_value")和get("intValue")获得的值相同，put进入的值也会被覆盖
 *
 * @author Looly
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @since 4.0.7
 */
public class UnderlineCaseInsensitiveMap<K, V> extends CustomKeyMap<K, V> {
    private static final long serialVersionUID = 4043263744224569870L;

    // ------------------------------------------------------------------------- Constructor start
    /**
     * 构造
     */
    public UnderlineCaseInsensitiveMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public UnderlineCaseInsensitiveMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public UnderlineCaseInsensitiveMap(Map<? extends K, ? extends V> m) {
        this(DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m Map
     */
    public UnderlineCaseInsensitiveMap(float loadFactor, Map<? extends K, ? extends V> m) {
        this(m.size(), loadFactor);
        this.putAll(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor 加载因子
     */
    public UnderlineCaseInsensitiveMap(int initialCapacity, float loadFactor) {
        super(new HashMap<K, V>(initialCapacity, loadFactor));
    }
    // ------------------------------------------------------------------------- Constructor end

    /**
     * 将Key转为下划线，如果key为字符串的话
     *
     * @param key KEY
     * @return 下划线风格Key
     */
    @Override
    protected Object customKey(Object key) {
        if (null != key && key instanceof CharSequence) {
            key = StrUtil.toUnderlineCase(key.toString()).toUpperCase();
        }
        return key;
    }

}

