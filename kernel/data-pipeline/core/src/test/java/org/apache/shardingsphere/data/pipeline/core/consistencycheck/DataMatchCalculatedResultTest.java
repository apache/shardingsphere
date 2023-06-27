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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataMatchCalculatedResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DataMatchCalculatedResultTest {
    
    @Test
    void assertEmptyRecordsEquals() {
        DataMatchCalculatedResult actual = new DataMatchCalculatedResult(0, Collections.emptyList());
        DataMatchCalculatedResult expected = new DataMatchCalculatedResult(0, Collections.emptyList());
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertFullTypeRecordsEquals() {
        DataMatchCalculatedResult actual = new DataMatchCalculatedResult(1000, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        DataMatchCalculatedResult expected = new DataMatchCalculatedResult(1000, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        assertThat(actual, is(expected));
    }
    
    private List<Object> buildFixedFullTypeRecord() {
        return Arrays.asList(true, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, 1.23F, 2.3456D, BigDecimal.valueOf(1.23456789D), "ok",
                new Time(123456789L), new Date(123456789L), new Timestamp(123456789L), new int[]{1, 2, 3}, null);
    }
    
    @Test
    void assertFullTypeRecordsEqualsWithDifferentDecimalScale() {
        DataMatchCalculatedResult expected = new DataMatchCalculatedResult(1000, Collections.singleton(buildFixedFullTypeRecord()));
        List<Object> record = buildFixedFullTypeRecord();
        for (int index = 0; index < record.size(); index++) {
            if (record.get(index) instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) record.get(index);
                record.set(index, decimal.setScale(decimal.scale() + 1, RoundingMode.CEILING));
            }
        }
        DataMatchCalculatedResult actual = new DataMatchCalculatedResult(1000, Collections.singleton(record));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertRecordsCountNotEquals() {
        DataMatchCalculatedResult result1 = new DataMatchCalculatedResult(1000, Collections.singleton(Collections.singleton(buildFixedFullTypeRecord())));
        DataMatchCalculatedResult result2 = new DataMatchCalculatedResult(1000, Collections.emptyList());
        assertNotEquals(result1, result2);
    }
    
    @Test
    void assertMaxUniqueKeyValueNotEquals() {
        DataMatchCalculatedResult result1 = new DataMatchCalculatedResult(1000, Collections.singleton(Collections.singleton(buildFixedFullTypeRecord())));
        DataMatchCalculatedResult result2 = new DataMatchCalculatedResult(1001, Collections.singleton(Collections.singleton(buildFixedFullTypeRecord())));
        assertNotEquals(result1, result2);
    }
    
    @Test
    void assertRandomColumnValueNotEquals() {
        List<Object> record = buildFixedFullTypeRecord();
        DataMatchCalculatedResult result1 = new DataMatchCalculatedResult(1000, Collections.singleton(record));
        for (int index = 0; index < record.size(); index++) {
            DataMatchCalculatedResult result2 = new DataMatchCalculatedResult(1000, Collections.singleton(modifyColumnValueRandomly(buildFixedFullTypeRecord(), index)));
            assertNotEquals(result1, result2);
        }
    }
    
    private List<Object> modifyColumnValueRandomly(final List<Object> record, final int index) {
        Object value = record.get(index);
        record.set(index, getModifiedValue(value));
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
