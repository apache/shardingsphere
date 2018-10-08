/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.core.resultset;

import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ResultSet utility class.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSetUtil {
    
    /**
     * Convert value via expected class type.
     * 
     * @param value original value
     * @param convertType expected class type
     * @return converted value
     */
    public static Object convertValue(final Object value, final Class<?> convertType) {
        if (null == value) {
            return convertNullValue(convertType);
        } 
        if (value.getClass() == convertType) {
            return value;
        }
        if (value instanceof Number) {
            return convertNumberValue(value, convertType);
        }
        if (value instanceof Date) {
            return convertDateValue(value, convertType);
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        } else {
            return value;
        }
    }
    
    private static Object convertNullValue(final Class<?> convertType) {
        switch (convertType.getName()) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "float":
                return 0F;
            case "double":
                return 0D;
            default:
                return null;
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
            case "java.lang.Object":
                return value;
            case "java.lang.String":
                return value.toString();
            default:
                throw new ShardingException("Unsupported data type:%s", convertType);
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
                throw new ShardingException("Unsupported Date type:%s", convertType);
        }
    }
}
