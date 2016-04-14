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

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 结果集处理工具类.
 * 
 * @author gaohongtao
 */
// TODO common包改名为util
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSetUtil {
    
    /**
     * 根据返回值类型返回特定类型的结果.
     * 
     * @param value 原始结果
     * @param convertType 返回值类型
     * @return 特定类型的返回结果
     */
    public static Object convertValue(final Object value, final Class<?> convertType) {
        // TODO 使用卫语句处理null和解除嵌套
        if (null == value) {
            // TODO 调整顺序, 按照调用顺序排列private方法
            return convertNullValue(convertType);
            // TODO class判断使用==
        } else if (value.getClass().equals(convertType)) {
            return value;
        } else if (value instanceof Number) {
            return convertNumberValue(value, convertType);
        } else if (value instanceof Date) {
            return convertDateValue(value, convertType);
        } else {
            if (String.class.equals(convertType)) {
                return value.toString();
            } else {
                return value;
            }
        }
    }
    
    private static Object convertDateValue(final Object value, final Class<?> convertType) {
        Date date = (Date) value;
        switch (convertType.getName()) {
            case "java.sql.Date":
                return new java.sql.Date(date.getTime());
            case "java.sql.Time":
                return new Time(date.getTime());
            case "java.sql.Timestamp":
                return new Timestamp(date.getTime());
            default:
                throw new ShardingJdbcException("Unsupported Date type:%s", convertType);
        }
    }
    
    private static Object convertNumberValue(final Object value, final Class<?> convertType) {
        Number number = (Number) value;
        switch (convertType.getName()) {
            case "byte":
                return number.byteValue();
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
                return new BigDecimal(number.toString());
            // TODO Object和String不是number, 不会进入switch
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
            case "byte":
                return (byte) 0;
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
            default:
                return null;
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
}
