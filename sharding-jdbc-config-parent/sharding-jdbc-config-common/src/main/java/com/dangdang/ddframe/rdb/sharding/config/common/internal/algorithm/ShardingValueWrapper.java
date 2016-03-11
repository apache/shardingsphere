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

package com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;

/**
 * 进入闭包的值进行包装,提供类型转换方法.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class ShardingValueWrapper {
    
    //value 可能的取值类型有 Number, java.sql.Date, String
    private final Comparable value;
    
    /**
     * 获取long值.
     * 
     * @return long型返回值
     */
    public long longValue() {
        return numberValue().longValue();
    }
    
    /**
     * 获取double值.
     * 
     * @return double型返回值
     */
    public double doubleValue() {
        return numberValue().doubleValue();
    }
    
    private Number numberValue() {
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof Date) {
            return ((Date) value).getTime();
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * 讲字符串转换为时间.
     * 
     * @param format 时间格式化格式
     * @return 时间类型的值
     * @throws ParseException
     */
    public Date dateValue(final String format) throws ParseException {
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        } else if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof String) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(format));
            return new SimpleDateFormat(format).parse((String) value);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    public Date dateValue() throws ParseException {
        return dateValue(null);
    }
    
    /**
     * 将时间类型的值转换为字符串.
     * 
     * @param format 时间格式化格式
     * @return 代表时间的字符串
     */
    public String toString(final String format) {
        if (value instanceof Date) {
            return new SimpleDateFormat(format).format(((Date) value).getTime());
        } else if (value instanceof Number) {
            return new SimpleDateFormat(format).format(((Number) value).longValue());
        } else {
            return toString();
        }
    }
    
    public String toString() {
        return value.toString();
    }
    
}
