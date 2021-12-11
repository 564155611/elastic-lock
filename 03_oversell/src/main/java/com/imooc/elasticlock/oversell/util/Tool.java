package com.imooc.elasticlock.oversell.util;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public final class Tool {
    public static boolean notTheSame(Object setVal, Object currentVal) {
        return (isNotNullOrEmpty(setVal) && !setVal.equals(currentVal)) || (isNullOrEmpty(setVal) && isNotNullOrEmpty(currentVal));
    }

    public static boolean isNullJson(String jsonStr) {
        return Tool.isNullOrEmpty(jsonStr) || "{}".equals(Tool.replaceAllSpace(jsonStr));
    }

    public static boolean isNotNullJson(String jsonStr) {
        return !Tool.isNullJson(jsonStr);
    }

    public static boolean isNotNullOrEmpty(Object obj) {
        return !Tool.isNullOrEmpty(obj);
    }

    /**
     * 判断对象或对象数组中每一个对象是否为空: 对象为null，字符序列长度为0，集合类、Map为empty
     *
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null || "null".equals(obj))
            return true;

        if (obj instanceof CharSequence)
            return ((CharSequence) obj).length() == 0;

        if (obj instanceof Collection)
            return ((Collection) obj).isEmpty();

        if (obj instanceof Map)
            return ((Map) obj).isEmpty();

        if (obj instanceof Object[]) {
            Object[] object = (Object[]) obj;
            if (object.length == 0) {
                return true;
            }
            boolean empty = true;
            for (int i = 0; i < object.length; i++) {
                if (!isNullOrEmpty(object[i])) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }
        return false;
    }


    /**
     * 字符串数组转以逗号分隔的字符串
     *
     * @return
     * @author kevin.xia
     */
    public static String array2String(Array arr) {
        StringBuffer tmp = new StringBuffer();
        if (arr != null) {
            for (int i = 0; i < Array.getLength(arr); i++) {
                Object obj = Array.get(arr, i);
                tmp.append(Tool.convertType(obj, String.class));
                if (i != Array.getLength(arr) - 1)
                    tmp.append(",");
            }
        } else {
            return "";
        }
        return tmp.toString();
    }

    public static String listOrSetToString(String split, Collection<?>... c) {
        if (Tool.isNullOrEmpty(c)) {
            return "";
        }
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < c.length; i++) {
            Collection<?> collection = c[i];
            if (!isNullOrEmpty(collection)) {
                for (Object str : collection) {
                    tmp.append(str);
                    tmp.append(split);
                }
                tmp = tmp.delete(tmp.lastIndexOf(split), tmp.length());
            }
        }
        return tmp.toString();
    }

    public static String listOrSetToStringWithoutEmpty(String split, Collection<?>... c) {
        StringBuffer tmp = new StringBuffer();
        if (Tool.isNullOrEmpty(c)) {
            return "";
        }
        for (int i = 0; i < c.length; i++) {
            Collection<?> collection = c[i];
            if (!isNullOrEmpty(collection)) {
                for (Object str : collection) {
                    if (Tool.isNotNullOrEmpty(str)) {
                        tmp.append(str);
                        tmp.append(split);
                    }
                }
                tmp = tmp.delete(tmp.lastIndexOf(split), tmp.length());
            }
        }
        return tmp.toString();
    }

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }


    public static <T> void sortMList(List<Map<String, T>> list, String... props) {
        final Set<String> propSet = new HashSet<>();
        for (String string : props) {
            propSet.add(string);
        }
        Collections.sort(list, new Comparator<Map<String, T>>() {

            @Override
            public int compare(Map<String, T> o1, Map<String, T> o2) {
                int ret = 0;
                Iterator<String> iterator = propSet.iterator();
                if (iterator.hasNext()) {
                    ret = doCompare(iterator.next(), iterator, o1, o2);
                }
                return ret;
            }

            @SuppressWarnings("unchecked")
            private int doCompare(String key1, Iterator<String> iterator, Map<String, T> o1,
                                  Map<String, T> o2) {
                int ret = 0;
                T value1 = o1.get(key1);
                T value2 = o2.get(key1);
                if (Tool.isNullOrEmpty(value1) && Tool.isNullOrEmpty(value2)) {
                    ret = 0;
                } else if (Tool.isNullOrEmpty(value1)) {
                    ret = -1;
                } else if (Tool.isNullOrEmpty(value2)) {
                    ret = 1;
                } else if (value2 instanceof Comparable<?>) {
                    ret = ((Comparable<T>) value1).compareTo(value2);
                } else {
                    ret = value1.hashCode() - value2.hashCode();
                }
                if (ret == 0) {
                    if (iterator.hasNext()) {
                        String key2 = iterator.next();
                        ret = doCompare(key2, iterator, o1, o2);
                    }
                }
                return ret;
            }
        });
    }

    /**
     * @param list
     * @param propOrderMap key为属性,value:ASC或者DESC
     * @author fanx
     * 2019年7月8日下午7:03:12
     */
    public static <T> void sortMList(List<Map<String, T>> list, final Map<String, String> propOrderMap) {
        if (Tool.isNullOrEmpty(propOrderMap)) {
            return;
        }
        final Set<String> propSet = propOrderMap.keySet();
        Collections.sort(list, new Comparator<Map<String, T>>() {

            @Override
            public int compare(Map<String, T> o1, Map<String, T> o2) {
                int ret = 0;
                Iterator<String> iterator = propSet.iterator();
                if (iterator.hasNext()) {
                    String next = iterator.next();
                    ret = doCompare(next, propOrderMap, iterator, o1, o2);
                }
                return ret;
            }

            @SuppressWarnings("unchecked")
            private int doCompare(String key1, Map<String, String> propOrderMap, Iterator<String> iterator, Map<String, T> o1,
                                  Map<String, T> o2) {
                int ret = 0;
                T value1 = o1.get(key1);
                T value2 = o2.get(key1);
                String orderType = propOrderMap.get(key1);
                if ("DESC".equalsIgnoreCase(orderType)) {
                    if (Tool.isNullOrEmpty(value1) && Tool.isNullOrEmpty(value2)) {
                        ret = 0;
                    } else if (Tool.isNullOrEmpty(value1)) {
                        ret = 1;
                    } else if (Tool.isNullOrEmpty(value2)) {
                        ret = -1;
                    } else if (value2 instanceof Comparable<?>) {
                        ret = ((Comparable<T>) value2).compareTo(value1);
                    } else {
                        ret = value2.hashCode() - value1.hashCode();
                    }
                } else {
                    if (Tool.isNullOrEmpty(value1) && Tool.isNullOrEmpty(value2)) {
                        ret = 0;
                    } else if (Tool.isNullOrEmpty(value1)) {
                        ret = -1;
                    } else if (Tool.isNullOrEmpty(value2)) {
                        ret = 1;
                    } else if (value2 instanceof Comparable<?>) {
                        ret = ((Comparable<T>) value1).compareTo(value2);
                    } else {
                        ret = value1.hashCode() - value2.hashCode();
                    }
                }
                if (ret == 0) {
                    if (iterator.hasNext()) {
                        String key2 = iterator.next();
                        ret = doCompare(key2, propOrderMap, iterator, o1, o2);
                    }
                }
                return ret;
            }
        });
    }

    /**
     * @param list
     * @param propOrderMap key为属性,value:ASC或者DESC
     * @author fanx
     * 2019年7月8日下午7:03:12
     */
    public static <T> void sortOList(List<T> list, final Map<String, String> propOrderMap) {
        if (Tool.isNullOrEmpty(propOrderMap)) {
            return;
        }
        final Set<String> propSet = propOrderMap.keySet();
        Collections.sort(list, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                int ret = 0;
                Iterator<String> iterator = propSet.iterator();
                if (iterator.hasNext()) {
                    String next = iterator.next();
                    try {
                        ret = doCompare(next, propOrderMap, iterator, o1, o2);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | IntrospectionException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                return ret;
            }

            @SuppressWarnings({"unchecked"})
            private int doCompare(String key1, Map<String, String> propOrderMap, Iterator<String> iterator, T o1,
                                  T o2) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException, NoSuchMethodException {
                int ret = 0;
                List<T> valueList = Tool.getValueList(o1, key1);
                List<T> valueList2 = Tool.getValueList(o2, key1);
                if (!Tool.isNullOrEmpty(valueList) && !Tool.isNullOrEmpty(valueList2)) {
                    T value1 = valueList.get(0);
                    T value2 = valueList2.get(0);
                    String orderType = propOrderMap.get(key1);

                    if ("DESC".equalsIgnoreCase(orderType)) {
                        if (Tool.isNullOrEmpty(value1) && Tool.isNullOrEmpty(value2)) {
                            ret = 0;
                        } else if (Tool.isNullOrEmpty(value1)) {
                            ret = 1;
                        } else if (Tool.isNullOrEmpty(value2)) {
                            ret = -1;
                        } else if (value2 instanceof Comparable<?>) {
                            ret = ((Comparable<T>) value2).compareTo(value1);
                        } else {
                            ret = value2.hashCode() - value1.hashCode();
                        }
                    } else {
                        if (Tool.isNullOrEmpty(value1) && Tool.isNullOrEmpty(value2)) {
                            ret = 0;
                        } else if (Tool.isNullOrEmpty(value1)) {
                            ret = -1;
                        } else if (Tool.isNullOrEmpty(value2)) {
                            ret = 1;
                        } else if (value2 instanceof Comparable<?>) {
                            ret = ((Comparable<T>) value1).compareTo(value2);
                        } else {
                            ret = value1.hashCode() - value2.hashCode();
                        }
                    }
                    if (ret == 0) {
                        if (iterator.hasNext()) {
                            String key2 = iterator.next();
                            ret = doCompare(key2, propOrderMap, iterator, o1, o2);
                        }
                    }
                }
                return ret;
            }
        });
    }

    public static <T> void sortOList(List<T> list, String... props) {
        final Set<String> propSet = new HashSet<>();
        for (String string : props) {
            propSet.add(string);
        }
        Collections.sort(list, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                int ret = 0;
                Iterator<String> iterator = propSet.iterator();
                if (iterator.hasNext()) {
                    try {
                        ret = doCompare(iterator.next(), iterator, o1, o2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ret;
            }

            @SuppressWarnings("unchecked")
            private int doCompare(String prop1, Iterator<String> iterator, T o1, T o2) throws Exception {
                int ret = 0;
                List<T> valueList = Tool.getValueList(o1, prop1);
                List<T> valueList2 = Tool.getValueList(o2, prop1);
                if (!Tool.isNullOrEmpty(valueList) && !Tool.isNullOrEmpty(valueList2)) {
                    T value1 = valueList.get(0);
                    T value2 = valueList2.get(0);
                    if (Tool.isNullOrEmpty(value1) && Tool.isNullOrEmpty(value2)) {
                        ret = 0;
                    } else if (Tool.isNullOrEmpty(value1)) {
                        ret = -1;
                    } else if (Tool.isNullOrEmpty(value2)) {
                        ret = 1;
                    } else if (value2 instanceof Comparable<?>) {
                        ret = ((Comparable<T>) value1).compareTo(value2);
                    } else {
                        ret = value1.hashCode() - value2.hashCode();
                    }
                    if (ret == 0) {
                        if (iterator.hasNext()) {
                            String prop2 = iterator.next();
                            ret = doCompare(prop2, iterator, o1, o2);
                        }
                    }
                }
                return ret;
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List getValueList(Object obj, String... fields) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List valueList = new ArrayList<>();
        for (String field : fields) {
            if (obj instanceof Map) {
                valueList.add(((Map<String, ?>) obj).get(field));
            } else {
                try {
                    Object val = PropertyUtils.getProperty(obj, field);
                    System.out.println(field + "=>" + val);
                    valueList.add(val);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return valueList;
    }

    @SuppressWarnings({"unchecked"})
    public static List<String> getStringValue(Object obj, String... fields) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<String> valueList = new ArrayList<>();
        for (String field : fields) {
            if (obj instanceof Map) {
                valueList.add(Tool.simpleObjectToString(((Map<String, ?>) obj).get(field)));
            } else {
                valueList.add(Tool.simpleObjectToString(PropertyUtils.getProperty(obj, field)));
            }
        }

        return valueList;
    }

    public static String simpleObjectToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof java.util.Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format(obj);
        }
        if (obj instanceof Calendar) {
            return new SimpleDateFormat("yyyy-MM-dd").format(((Calendar) obj).getTime());
        }
        if (obj instanceof List || obj instanceof Set || obj instanceof Map || obj.getClass().isArray()) {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return obj.toString();
    }

    /**
     * 获取两个日期之间的所有日期集合
     *
     * @return
     * @author fanx 2018年7月10日下午3:19:26
     */
    public static List<String> getAllDates(Calendar cal1, Calendar cal2) {
        List<String> dateList = new ArrayList<>();
        Calendar _cal = (Calendar) cal1.clone();
        while (_cal.compareTo(cal2) <= 0) {
            dateList.add(new SimpleDateFormat("yyyy-MM-dd").format(((Calendar) _cal.clone()).getTime()));
            _cal.add(Calendar.DATE, 1);
        }
        return dateList;
    }

    public static List<String> getAllDates(Calendar cal1, Calendar cal2, String format) {
        List<String> dateList = new ArrayList<>();
        Calendar _cal = (Calendar) cal1.clone();
        while (_cal.compareTo(cal2) <= 0) {
            dateList.add(new SimpleDateFormat(format).format(((Calendar) _cal.clone()).getTime()));
            _cal.add(Calendar.DATE, 1);
        }
        return dateList;
    }

    public static java.util.Date dateAdd(java.util.Date d, int calendarField, int amount) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(d);
        instance.add(calendarField, amount);
        return instance.getTime();
    }

    public static String convertMM_dd2yyyy_MM_dd(String MM_dd) {
        java.util.Date nDate = new java.util.Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(nDate);
        Calendar beginCal = Calendar.getInstance();
        try {
            beginCal.setTime(new SimpleDateFormat("MM-dd").parse(MM_dd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int i = instance.get(Calendar.YEAR);
        beginCal.set(Calendar.YEAR, i);
        return new SimpleDateFormat("yyyy-MM-dd").format(beginCal.getTime());
    }


    /**
     * 从Map的多个可替换Key中获取有效的key对应的值,并且将对应空值的key赋值
     *
     * @param dataMap
     * @param keys
     * @return
     * @author fanx
     * 2018年10月8日下午3:06:08
     */
    public static Object getRightFirstValueFromMap(Map<String, Object> dataMap, String... keys) {
        if (Tool.isNullOrEmpty(dataMap) || Tool.isNullOrEmpty(keys)) {
            return null;
        }
        Set<String> keySet = dataMap.keySet();
        for (String key : keys) {
            if (keySet.contains(key)) {
                Object val = dataMap.get(key);
                if (!Tool.isNullOrEmpty(val)) {
                    for (String k : keys) {
                        dataMap.put(k, val);
                    }
                    return val;
                }
            }
        }
        return null;
    }

    public static String replaceAllSpace(String s) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        return s.replace(" ", "").replaceAll("[ ]*", "").replace("\n", "").replaceAll((char) 12288 + "", "");
    }


    public static List<Integer> indexOf(CharSequence sqlStatement, CharSequence... childPartSequences) {
        StringBuilder sqlStatementBuilder = new StringBuilder(String.valueOf(sqlStatement));
        List<Integer> locList = new ArrayList<>();
        for (CharSequence charSequence : childPartSequences) {
            locList.add(sqlStatementBuilder.indexOf(charSequence.toString()));
        }
        return locList;
    }

    /**
     * 获取多个字符的位置的集合,可配合com.ehr.util.Tool.getTheRightFirstLocOfString(List<Integer>)一起筛选出最先出现的那个字符
     *
     * @param sqlStatement
     * @param after              在after之后开始寻找
     * @param childPartSequences
     * @return
     * @author fanx
     * 2018年9月9日下午6:10:32
     */
    public static List<Integer> indexOf(CharSequence sqlStatement, int after, CharSequence... childPartSequences) {
        List<Integer> locList = new ArrayList<>();
        if (after < 0) {
            for (CharSequence charSequence : childPartSequences) {
                locList.add(-1);
            }
        }
        StringBuilder sqlStatementBuilder = new StringBuilder(String.valueOf(sqlStatement));
        for (CharSequence charSequence : childPartSequences) {
            locList.add(sqlStatementBuilder.indexOf(charSequence.toString(), after));
        }
        return locList;
    }


    public static Map<String, Object> mergeMapWithoutNull(Map<String, Object> oneMap, Map<String, Object> anotherMap) {
        Set<String> keySet = new HashSet<>(anotherMap.keySet());
        Set<String> keySet2 = new HashSet<>(oneMap.keySet());
        Map<String, Object> ret = new HashMap<>();
        keySet.addAll(keySet2);
        for (String key : keySet) {
            Object anotherObj = anotherMap.get(key);
            Object oneObj = oneMap.get(key);
            Object ele = Tool.isNullOrEmpty(oneObj) ? anotherObj : oneObj;
            ret.put(key, ele);
        }
        return ret;
    }

    public static <T> Collection<T> convertCollectionType(Collection<?> col, Class<T> clazz) {
        if (col == null) {
            return null;
        }
        if (col.size() == 0) {
            return new ArrayList<T>();
        }
        return col.stream().map(t -> Tool.convertType(t, clazz)).collect(Collectors.toList());
    }

    public static <T> T[] convertArrayType(Object arr, Class<T> clazz) {
        if (arr == null) {
            return null;
        }
        if (!ArrayUtil.isArray(arr)) {
            T[] ret = (T[]) Array.newInstance(clazz, 1);
            ret[0] = Tool.convertType(arr, clazz);
            return ret;
        }
        int length = Array.getLength(arr);
        T[] ret = (T[]) Array.newInstance(clazz, length);
        if (length == 0) {
            return ret;
        }
        for (int i = 0; i < length; i++) {
            Object o = Array.get(arr, i);
            ret[i] = Tool.convertType(o, clazz);
        }
        return ret;

    }

    @SuppressWarnings("unchecked")
    public static <T> T convertType(Object orignValue, Class<T> clazz) {
        if (orignValue == null) {
            return null;
        }
        if (orignValue.getClass().isAssignableFrom(clazz)) {
            return (T) orignValue;
        }
        //clazz为String.class
        if (String.class.isAssignableFrom(clazz)) {
            return (T) Tool.simpleObjectToString(orignValue);
        }
        //clazz为StringBuilder.class
        if (StringBuilder.class.isAssignableFrom(clazz)) {
            String stringValue = Tool.simpleObjectToString(orignValue);
            if (Tool.isNotNullOrEmpty(stringValue)) {
                return (T) new StringBuilder(stringValue);
            }
            return (T) new StringBuilder();
        }
        //clazz为StringBuffer.class
        if (StringBuffer.class.isAssignableFrom(clazz)) {
            String stringValue = Tool.simpleObjectToString(orignValue);
            if (Tool.isNotNullOrEmpty(stringValue)) {
                return (T) new StringBuffer(stringValue);
            }
            return (T) new StringBuffer();
        }
        //clazz为Integer.class类型
        if (Integer.class.isAssignableFrom(clazz)) {
            String stringValue = Tool.simpleObjectToString(orignValue);
            if (Tool.isNotNullOrEmpty(stringValue)) {
                return (T) Integer.valueOf(stringValue);
            }
            return null;
        }
        //clazz为Long类型
        if (Long.class.isAssignableFrom(clazz)) {
            String stringValue = Tool.simpleObjectToString(orignValue);
            if (Tool.isNotNullOrEmpty(stringValue)) {
                return (T) Long.valueOf(stringValue);
            }
            return null;
        }
        //clazz为Long类型
        if (BigDecimal.class.isAssignableFrom(clazz)) {
            if (orignValue instanceof BigDecimal) {
                return (T) ((BigDecimal) orignValue).setScale(2, RoundingMode.HALF_UP);
            }
            String stringValue = Tool.simpleObjectToString(orignValue);
            if (Tool.isNotNullOrEmpty(stringValue)) {
                return (T) new BigDecimal(stringValue).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            return null;
        }
        //clazz为java.util.Date类型
        if (java.util.Date.class.isAssignableFrom(clazz)) {
            String stringValue = Tool.simpleObjectToString(orignValue);
            if (Tool.isNullOrEmpty(stringValue)) {
                return null;
            }
            Date date = null;
            if (stringValue.length() == 19) {
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (stringValue.length() == 10) {
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                    try {
                        date = new SimpleDateFormat("yyyy/MM/dd").parse(stringValue);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (stringValue.length() == 8) {
                try {
                    date = new SimpleDateFormat("yyyyMMdd").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (stringValue.length() == 7) {
                try {
                    date = new SimpleDateFormat("yyyy-MM").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                    try {
                        date = new SimpleDateFormat("yyyy/MM").parse(stringValue);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (stringValue.length() == 6) {
                try {
                    date = new SimpleDateFormat("yyyyMM").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (stringValue.length() == 5) {
                try {
                    date = new SimpleDateFormat("MM-dd").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                    try {
                        date = new SimpleDateFormat("MM/dd").parse(stringValue);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (stringValue.length() == 4) {
                try {
                    date = new SimpleDateFormat("MMdd").parse(stringValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return (T) date;
        }
        return (T) orignValue;
    }

    //去除重复数据
    @SuppressWarnings("unchecked")
    public static <T> List<T> getUnrepeatListWithOverride(List<T> sourceList, String... uniqueProps) {
        if (Tool.isNullOrEmpty(sourceList)) {
            return sourceList;
        }
        Map<String, Object> map = new HashMap<>();
        for (Object object : sourceList) {
            try {
                @SuppressWarnings("rawtypes")
                List valueList = Tool.getValueList(object, uniqueProps);
                String key = Tool.listOrSetToString("/", valueList);
                map.put(key, object);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return (List<T>) new ArrayList<>(map.values());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getUnrepeatListWithOverrideNotNull(List<T> sourceList, String... uniqueProps) {
        if (Tool.isNullOrEmpty(sourceList)) {
            return sourceList;
        }
        Map<String, Object> map = new HashMap<>();
        for (Object object : sourceList) {
            try {
                @SuppressWarnings("rawtypes")
                List valueList = Tool.getValueList(object, uniqueProps);
                if (valueList.contains(null)) {
                    continue;
                }
                String key = Tool.listOrSetToString("/", valueList);
                map.put(key, object);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return (List<T>) new ArrayList<>(map.values());
    }

    public static List<Object> getUnrepeatList(List<Object> sourceList, String... uniqueProps) {
        if (Tool.isNullOrEmpty(sourceList)) {
            return sourceList;
        }
        Map<String, Object> map = new HashMap<>();
        for (Object object : sourceList) {
            try {
                @SuppressWarnings("rawtypes")
                List valueList = Tool.getValueList(object, uniqueProps);
                String key = Tool.listOrSetToString("/", valueList);
                if (!map.keySet().contains(key)) {
                    map.put(key, object);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(map.values());
    }

    public static List<Object> getUnrepeatListNotNull(List<Object> sourceList, String... uniqueProps) {
        if (Tool.isNullOrEmpty(sourceList)) {
            return sourceList;
        }
        Map<String, Object> map = new HashMap<>();
        for (Object object : sourceList) {
            try {
                @SuppressWarnings("rawtypes")
                List valueList = Tool.getValueList(object, uniqueProps);
                if (valueList.contains(null)) {
                    continue;
                }
                String key = Tool.listOrSetToString("/", valueList);
                if (!map.keySet().contains(key)) {
                    map.put(key, object);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(map.values());
    }

    public static <T> Map<String, String> getMapByList(Iterable<T> objList, String[] keyFields,
                                                       String[] valueFields) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException, NoSuchMethodException, SecurityException {
        Map<String, String> keyValueMap = new LinkedHashMap<>();
        if (Tool.isNotNullOrEmpty(objList)) {
            for (T obj : objList) {
                StringBuilder keyStr = new StringBuilder();
                StringBuilder valueStr = new StringBuilder();
                if (Tool.isNotNullOrEmpty(keyFields)) {
                    for (String field : keyFields) {
                        if (Tool.isNullOrEmpty(field)) {
                            continue;
                        }
                        List<String> keyList = Tool.getStringValue(obj, field);
                        if (Tool.isNotNullOrEmpty(keyList) && Tool.isNotNullOrEmpty(keyList.get(0))) {
                            String key = keyList.get(0);
                            keyStr.append(key);
                        }
                        keyStr.append("/");
                    }
                    keyStr = keyStr.deleteCharAt(keyStr.length() - 1);
                }
                if (Tool.isNotNullOrEmpty(valueFields)) {
                    for (String field : valueFields) {
                        if (Tool.isNullOrEmpty(field)) {
                            continue;
                        }
                        List<String> valueList = Tool.getStringValue(obj, field);
                        if (Tool.isNotNullOrEmpty(valueList) && Tool.isNotNullOrEmpty(valueList.get(0))) {
                            String value = valueList.get(0);
                            valueStr.append(value);
                        }
                        valueStr.append("/");


                    }
                    valueStr = valueStr.deleteCharAt(valueStr.length() - 1);
                }
                keyValueMap.put(keyStr.toString(), valueStr.toString());
            }
        }
        return keyValueMap;
    }

    public static <T> Map<String, List<String>> getMultiValueMapByList(List<T> objList, String[] keyFields,
                                                                       String[] valueFields) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException, NoSuchMethodException, SecurityException {
        Map<String, List<String>> keyValueListMap = new HashMap<>();
        for (T obj : objList) {
            StringBuilder keyStr = new StringBuilder();
            StringBuilder valueStr = new StringBuilder();
            if (Tool.isNotNullOrEmpty(keyFields)) {
                for (String field : keyFields) {
                    if (Tool.isNullOrEmpty(field)) {
                        continue;
                    }
                    List<String> keyList = Tool.getStringValue(obj, field);
                    if (Tool.isNotNullOrEmpty(keyList) && Tool.isNotNullOrEmpty(keyList.get(0))) {
                        String key = keyList.get(0);
                        keyStr.append(key);
                    }
                    keyStr.append("/");
                }
                keyStr = keyStr.deleteCharAt(keyStr.length() - 1);
            }
            if (Tool.isNotNullOrEmpty(valueFields)) {
                for (String field : valueFields) {
                    if (Tool.isNullOrEmpty(field)) {
                        continue;
                    }
                    List<String> valueList = Tool.getStringValue(obj, field);
                    if (Tool.isNotNullOrEmpty(valueList) && Tool.isNotNullOrEmpty(valueList.get(0))) {
                        String value = valueList.get(0);
                        valueStr.append(value);
                    }
                    valueStr.append("/");


                }
                valueStr = valueStr.deleteCharAt(valueStr.length() - 1);
            }
            String key = keyStr.toString();
            if (!keyValueListMap.containsKey(key)) {
                keyValueListMap.put(key, new ArrayList<String>());
            }
            keyValueListMap.get(key).add(valueStr.toString());
        }
        return keyValueListMap;
    }

    public static <T> Map<String, T> getIndexMapByList(List<T> objList, String... props) {
        Map<String, T> retMap = new HashMap<>();
        for (T obj : objList) {
            StringBuilder keyStr = new StringBuilder();
            if (Tool.isNotNullOrEmpty(props)) {
                for (String field : props) {
                    if (Tool.isNullOrEmpty(field)) {
                        continue;
                    }
                    List<String> keyList = new ArrayList<>();
                    try {
                        keyList = Tool.getStringValue(obj, field);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                    if (Tool.isNotNullOrEmpty(keyList) && Tool.isNotNullOrEmpty(keyList.get(0))) {
                        String key = keyList.get(0);
                        keyStr.append(key);
                    }
                    keyStr.append("/");
                }
                keyStr = keyStr.deleteCharAt(keyStr.length() - 1);
            }
            retMap.put(keyStr.toString(), obj);
        }
        return retMap;
    }

    public static <T> Map<String, List<T>> getIndexMultiValueMapByList(List<T> objList, String... props) {
        Map<String, List<T>> retMap = new LinkedHashMap<>();
        for (T obj : objList) {
            StringBuilder keyStr = new StringBuilder();
            if (Tool.isNotNullOrEmpty(props)) {
                for (String field : props) {
                    if (Tool.isNullOrEmpty(field)) {
                        continue;
                    }
                    List<String> keyList = new ArrayList<>();
                    try {
                        keyList = Tool.getStringValue(obj, field);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | SecurityException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    if (Tool.isNotNullOrEmpty(keyList) && Tool.isNotNullOrEmpty(keyList.get(0))) {
                        String key = keyList.get(0);
                        keyStr.append(key);
                    }
                    keyStr.append("/");
                }
                keyStr = keyStr.deleteCharAt(keyStr.length() - 1);
            }
            if (!retMap.containsKey(keyStr.toString())) {
                retMap.put(keyStr.toString(), new ArrayList<T>());
            }
            retMap.get(keyStr.toString()).add(obj);
        }
        return retMap;
    }

    public static Set<String> getKeySetByList(List<?> objList, String... props) throws NoSuchMethodException, SecurityException {
        Set<String> retSet = new HashSet<>();
        for (Object obj : objList) {
            StringBuilder keyStr = new StringBuilder();
            if (Tool.isNotNullOrEmpty(props)) {
                for (String field : props) {
                    if (Tool.isNullOrEmpty(field)) {
                        continue;
                    }
                    List<String> keyList = new ArrayList<>();
                    try {
                        keyList = Tool.getStringValue(obj, field);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (Tool.isNotNullOrEmpty(keyList) && Tool.isNotNullOrEmpty(keyList.get(0))) {
                        String key = keyList.get(0);
                        keyStr.append(key);
                    }
                    keyStr.append("/");
                }
                keyStr = keyStr.deleteCharAt(keyStr.length() - 1);
            }
            retSet.add(keyStr.toString());
        }
        return retSet;
    }


    @SuppressWarnings("unchecked")
    public static <T> T getByKey(Map<String, ?> someMap, String queryKey, String filterType) {
        Set<String> keySet = someMap.keySet();
        List<Object> retList = new ArrayList<>();
        switch (filterType) {
            case "eq":
                return (T) someMap.get(queryKey);
            case "ne":
                Map<String, Object> nMap = new HashMap<>(someMap);
                nMap.remove(queryKey);
                return (T) nMap.values();
            case "gt":
                for (String key : keySet) {
                    if (key.compareTo(queryKey) > 0) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "lt":
                for (String key : keySet) {
                    if (key.compareTo(queryKey) < 0) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "le":
                for (String key : keySet) {
                    if (key.compareTo(queryKey) <= 0) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "ge":
                for (String key : keySet) {
                    if (key.compareTo(queryKey) >= 0) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "contain":
                for (String key : keySet) {
                    if (key.contains(queryKey)) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "notContain":
                for (String key : keySet) {
                    if (!key.contains(queryKey)) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "start":
                for (String key : keySet) {
                    if (key.startsWith(queryKey)) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            case "end":
                for (String key : keySet) {
                    if (key.endsWith(queryKey)) {
                        retList.add(someMap.get(key));
                    }
                }
                return (T) retList;
            default:
                return (T) someMap.get(queryKey);
        }
    }

}

class MapKeyComparator implements Comparator<String> {

    public int compare(String str1, String str2) {

        return str1.compareTo(str2);
    }
}
