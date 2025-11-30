/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.core.consistencycheck;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsistencyCheckDataBuilder {
    
    /**
     * Build fixed full type record.
     *
     * @param id id
     * @return built record
     */
    public static Map<String, Object> buildFixedFullTypeRecord(final int id) {
        Map<String, Object> result = new LinkedHashMap<>(15, 1F);
        result.put("id", id);
        result.put("c_bool", true);
        result.put("c_int1", Byte.MAX_VALUE);
        result.put("c_int2", Short.MAX_VALUE);
        result.put("c_int4", Integer.MAX_VALUE);
        result.put("c_int8", Long.MAX_VALUE);
        result.put("c_float", 1.23F);
        result.put("c_double", 2.3456D);
        result.put("c_decimal", BigDecimal.valueOf(1.23456789D));
        result.put("c_varchar", "ok");
        result.put("c_time", new Time(123456789L));
        result.put("c_date", new Date(123456789L));
        result.put("c_timestamp", new Timestamp(123456789L));
        result.put("c_array", new int[]{1, 2, 3});
        result.put("c_blob", null);
        return result;
    }
    
    /**
     * Modify column value randomly.
     *
     * @param record record
     * @param key which key will be modified
     * @return original record
     */
    public static Map<String, Object> modifyColumnValueRandomly(final Map<String, Object> record, final String key) {
        record.compute(key, (toBeModifiedKey, value) -> getRandomlyModifiedValue(value));
        return record;
    }
    
    private static Object getRandomlyModifiedValue(final Object value) {
        if (null == value) {
            return new Object();
        }
        if (value instanceof Boolean) {
            return !((Boolean) value);
        }
        if (value instanceof Byte) {
            return (byte) ((Byte) value - 1);
        }
        if (value instanceof Short) {
            return (short) ((Short) value - 1);
        }
        if (value instanceof Integer) {
            return (Integer) value - 1;
        }
        if (value instanceof Long) {
            return (Long) value - 1L;
        }
        if (value instanceof Float) {
            return (Float) value - 1F;
        }
        if (value instanceof Double) {
            return (Double) value - 1D;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).subtract(BigDecimal.ONE);
        }
        if (value instanceof String) {
            return value + "-";
        }
        if (value instanceof Time) {
            return new Time(((Time) value).getTime() - 1L);
        }
        if (value instanceof Date) {
            return new Date(((Date) value).getTime() - 1L);
        }
        if (value instanceof Timestamp) {
            return new Timestamp(((Timestamp) value).getTime() - 1000L);
        }
        if (value instanceof int[]) {
            int[] result = ((int[]) value).clone();
            result[0] = result[0] - 1;
            return result;
        }
        return value;
    }
}
