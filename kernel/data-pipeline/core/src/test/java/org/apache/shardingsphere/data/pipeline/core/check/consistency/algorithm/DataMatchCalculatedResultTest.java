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

package org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm;

import org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm.DataMatchDataConsistencyCalculateAlgorithm.CalculatedResult;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class DataMatchCalculatedResultTest {
    
    @Test
    public void assertEmptyRecordsEquals() {
        CalculatedResult result1 = new CalculatedResult(0, 0, Collections.emptyList());
        CalculatedResult result2 = new CalculatedResult(0, 0, Collections.emptyList());
        assertEquals(result1, result2);
    }
    
    @Test
    public void assertFullTypeRecordsEquals() {
        CalculatedResult result1 = new CalculatedResult(1000, 2, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        CalculatedResult result2 = new CalculatedResult(1000, 2, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        assertEquals(result1, result2);
    }
    
    private List<Object> buildFixedFullTypeRecord() {
        return Arrays.asList(true, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, 1.23F, 2.3456D, BigDecimal.valueOf(1.23456789D), "ok",
                new Time(123456789L), new Date(123456789L), new Timestamp(123456789L), new int[]{1, 2, 3}, null);
    }
    
    @Test
    public void assertFullTypeRecordsEqualsWithDifferentDecimalScale() {
        CalculatedResult result1 = new CalculatedResult(1000, 1, Collections.singletonList(buildFixedFullTypeRecord()));
        List<Object> record = buildFixedFullTypeRecord();
        for (int index = 0; index < record.size(); index++) {
            if (record.get(index) instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) record.get(index);
                record.set(index, decimal.setScale(decimal.scale() + 1, RoundingMode.CEILING));
            }
        }
        CalculatedResult result2 = new CalculatedResult(1000, 1, Collections.singletonList(record));
        assertEquals(result1, result2);
    }
    
    @Test
    public void assertRecordsCountNotEquals() {
        CalculatedResult result1 = new CalculatedResult(1000, 1, Collections.emptyList());
        CalculatedResult result2 = new CalculatedResult(1000, 0, Collections.emptyList());
        assertNotEquals(result1, result2);
    }
    
    @Test
    public void assertMaxUniqueKeyValueNotEquals() {
        CalculatedResult result1 = new CalculatedResult(1000, 1, Collections.emptyList());
        CalculatedResult result2 = new CalculatedResult(1001, 1, Collections.emptyList());
        assertNotEquals(result1, result2);
    }
    
    @Test
    public void assertRandomColumnValueNotEquals() {
        List<Object> record = buildFixedFullTypeRecord();
        CalculatedResult result1 = new CalculatedResult(1000, 1, Collections.singletonList(record));
        for (int index = 0; index < record.size(); index++) {
            CalculatedResult result2 = new CalculatedResult(1000, 1, Collections.singletonList(modifyColumnValueRandomly(buildFixedFullTypeRecord(), index)));
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
