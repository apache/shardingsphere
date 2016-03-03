/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.common;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 结果集处理工具类.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSetUtil {
    
    /**
     * 从结果集中提取结果值.
     * 
     * @param groupByColumn 分组列对象
     * @param resultSet 目标结果集
     * @return 结果对象
     * @throws SQLException
     */
    public static Object getValue(final GroupByColumn groupByColumn, final ResultSet resultSet) throws SQLException {
        Object result = getValue(groupByColumn.getAlias(), resultSet);
        Preconditions.checkNotNull(result);
        return result;
    }
    
    /**
     * 从结果集中提取结果值.
     * 
     * @param orderByColumn 排序列对象
     * @param resultSet 目标结果集
     * @return 结果对象
     * @throws SQLException
     */
    public static Object getValue(final OrderByColumn orderByColumn, final ResultSet resultSet) throws SQLException {
        Object result = null;
        if (orderByColumn.getIndex().isPresent()) {
            result = resultSet.getObject(orderByColumn.getIndex().get());
        } else if (orderByColumn.getAlias().isPresent()) {
            result = getValue(orderByColumn.getAlias().get(), resultSet);
        } else if (orderByColumn.getName().isPresent()) {
            result = getValue(orderByColumn.getName().get(), resultSet);
        }
        Preconditions.checkNotNull(result);
        return result;
    }
    
    private static Object getValue(final String columnName, final ResultSet resultSet) throws SQLException {
        Object result = resultSet.getObject(columnName);
        if (null == result) {
            result = resultSet.getObject(columnName.toUpperCase());
        }
        if (null == result) {
            result = resultSet.getObject(columnName.toLowerCase());
        }
        return result;
    }
    
    /**
     * 根据返回值类型返回特定类型的结果.
     * 
     * @param value 原始结果
     * @param convertType 返回值类型
     * @return 特定类型的返回结果
     */
    public static Object convertValue(final Object value, final Class<?> convertType) {
        if (null == value) {
            return convertNullValue(convertType);
        } else if (value instanceof Number) {
            return convertNumberValue(value, convertType);
        } else {
            if (String.class.equals(convertType)) {
                return value.toString();
            } else {
                return value;
            }
        }
    }
    
    private static Object convertNumberValue(final Object value, final Class<?> convertType) {
        Number number = (Number) value;
        switch (convertType.getName()) {
            case "short":
                return number.shortValue();
            case "int":
                return number.intValue();
            case "long":
                return number.longValue();
            case "double":
                return number.doubleValue();
            case "float":
                return number.floatValue();
            case "java.math.BigDecimal":
                if (number instanceof BigDecimal) {
                    return number;
                } else {
                    return new BigDecimal(number.toString());
                }
            case "java.lang.Object":
                return value;
            case "java.lang.String":
                return value.toString();
            default:
                throw new ShardingJdbcException("Unsupported data type:%s", convertType);
        }
    }
    
    private static Object convertNullValue(final Class<?> convertType) {
        switch (convertType.getName()) {
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "double":
                return 0D;
            case "float":
                return 0F;
            case "java.math.BigDecimal":
            case "java.lang.Object":
            case "java.lang.String":
                return null;
            default:
                throw new ShardingJdbcException("Unsupported data type:%s", convertType);
        }
    }
    
    /**
     * 根据排序类型比较大小.
     * 
     * @param thisValue 待比较的值
     * @param otherValue 待比较的值
     * @param orderByType 排序类型
     * @return 负数，零和正数分别表示小于，等于和大于
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static int compareTo(final Comparable thisValue, final Comparable otherValue, final OrderByType orderByType) {
        return OrderByType.ASC == orderByType ? thisValue.compareTo(otherValue) : -thisValue.compareTo(otherValue);
    }
    
    /**
     * 向聚合列的补列填充索引值.
     * 
     * @param resultSet 结果集对象
     * @param aggregationColumns 聚合列集合
     * @throws SQLException SQL异常
     */
    public static void fillIndexesForDerivedAggregationColumns(final ResultSet resultSet, final Collection<AggregationColumn> aggregationColumns) throws SQLException {
        for (AggregationColumn aggregationColumn : aggregationColumns) {
            for (AggregationColumn derivedColumn : aggregationColumn.getDerivedColumns()) {
                derivedColumn.setIndex(resultSet.findColumn(derivedColumn.getAlias().get()));
            }
        }
    }
}
