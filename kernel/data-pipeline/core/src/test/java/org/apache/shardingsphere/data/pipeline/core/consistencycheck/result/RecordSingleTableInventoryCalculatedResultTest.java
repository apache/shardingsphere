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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.result;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RecordSingleTableInventoryCalculatedResultTest {
    
    @Test
    void assertEmptyRecordsEquals() {
        RecordSingleTableInventoryCalculatedResult actual = new RecordSingleTableInventoryCalculatedResult(0, Collections.emptyList());
        RecordSingleTableInventoryCalculatedResult expected = new RecordSingleTableInventoryCalculatedResult(0, Collections.emptyList());
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertFullTypeRecordsEquals() {
        RecordSingleTableInventoryCalculatedResult actual = new RecordSingleTableInventoryCalculatedResult(1000, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        RecordSingleTableInventoryCalculatedResult expected = new RecordSingleTableInventoryCalculatedResult(1000, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        assertThat(actual, is(expected));
    }
    
    private Map<String, Object> buildFixedFullTypeRecord() {
        Map<String, Object> result = new LinkedHashMap<>();
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
    
    @Test
    void assertFullTypeRecordsEqualsWithDifferentDecimalScale() {
        RecordSingleTableInventoryCalculatedResult expected = new RecordSingleTableInventoryCalculatedResult(1000, Collections.singletonList(buildFixedFullTypeRecord()));
        Map<String, Object> record = buildFixedFullTypeRecord();
        record.forEach((key, value) -> {
            if (value instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) value;
                record.put(key, decimal.setScale(decimal.scale() + 1, RoundingMode.CEILING));
            }
        });
        RecordSingleTableInventoryCalculatedResult actual = new RecordSingleTableInventoryCalculatedResult(1000, Collections.singletonList(record));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertRecordsCountNotEquals() {
        RecordSingleTableInventoryCalculatedResult result1 = new RecordSingleTableInventoryCalculatedResult(1000, Collections.singletonList(buildFixedFullTypeRecord()));
        RecordSingleTableInventoryCalculatedResult result2 = new RecordSingleTableInventoryCalculatedResult(1000, Collections.emptyList());
        assertNotEquals(result1, result2);
    }
    
    @Test
    void assertMaxUniqueKeyValueNotEquals() {
        RecordSingleTableInventoryCalculatedResult result1 = new RecordSingleTableInventoryCalculatedResult(1000, Collections.singletonList(buildFixedFullTypeRecord()));
        RecordSingleTableInventoryCalculatedResult result2 = new RecordSingleTableInventoryCalculatedResult(1001, Collections.singletonList(buildFixedFullTypeRecord()));
        assertNotEquals(result1, result2);
    }
    
    @Test
    void assertRandomColumnValueNotEquals() {
        Map<String, Object> record = buildFixedFullTypeRecord();
        RecordSingleTableInventoryCalculatedResult result1 = new RecordSingleTableInventoryCalculatedResult(1000, Collections.singletonList(record));
        record.forEach((key, value) -> {
            RecordSingleTableInventoryCalculatedResult result2 = new RecordSingleTableInventoryCalculatedResult(1000,
                    Collections.singletonList(modifyColumnValueRandomly(buildFixedFullTypeRecord(), key)));
            assertNotEquals(result1, result2);
        });
    }
    
    private Map<String, Object> modifyColumnValueRandomly(final Map<String, Object> record, final String key) {
        Object value = record.get(key);
        record.put(key, getModifiedValue(value));
        return record;
    }
    
    private Object getModifiedValue(final Object value) {
        if (null == value) {
            return new Object();
        }
        if (value instanceof Boolean) {
            return !((Boolean) value);
        }
        if (value instanceof Byte) {
            return (Byte) value - 1;
        }
        if (value instanceof Short) {
            return (Short) value - 1;
        }
        if (value instanceof Integer) {
            return (Integer) value - 1;
        }
        if (value instanceof Long) {
            return (Long) value - 1;
        }
        if (value instanceof Float) {
            return (Float) value - 1;
        }
        if (value instanceof Double) {
            return (Double) value - 1;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).subtract(BigDecimal.ONE);
        }
        if (value instanceof String) {
            return value + "-";
        }
        if (value instanceof Time) {
            return new Time(((Time) value).getTime() - 1);
        }
        if (value instanceof Date) {
            return new Date(((Date) value).getTime() - 1);
        }
        if (value instanceof Timestamp) {
            return new Timestamp(((Timestamp) value).getTime() - 1);
        }
        if (value instanceof int[]) {
            int[] result = ((int[]) value).clone();
            result[0] = result[0] - 1;
            return result;
        }
        return value;
    }
}
