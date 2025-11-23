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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckDataBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RecordTableInventoryCheckCalculatedResultTest {
    
    @Test
    void assertNotEqualsWithNull() {
        assertFalse(new RecordTableInventoryCheckCalculatedResult(0, Collections.emptyList()).equals(null));
    }
    
    @Test
    void assertEqualsWithSameObject() {
        RecordTableInventoryCheckCalculatedResult calculatedResult = new RecordTableInventoryCheckCalculatedResult(0, Collections.emptyList());
        assertThat(calculatedResult, is(calculatedResult));
    }
    
    @Test
    void assertNotEqualsWithDifferentClassType() {
        RecordTableInventoryCheckCalculatedResult actual = new RecordTableInventoryCheckCalculatedResult(0, Collections.emptyList());
        Object expected = new Object();
        assertThat(actual, not(expected));
    }
    
    @Test
    void assertEqualsWithEmptyRecords() {
        RecordTableInventoryCheckCalculatedResult actual = new RecordTableInventoryCheckCalculatedResult(0, Collections.emptyList());
        RecordTableInventoryCheckCalculatedResult expected = new RecordTableInventoryCheckCalculatedResult(0, Collections.emptyList());
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEqualsWithFullTypeRecords() {
        RecordTableInventoryCheckCalculatedResult actual = new RecordTableInventoryCheckCalculatedResult(1000, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        RecordTableInventoryCheckCalculatedResult expected = new RecordTableInventoryCheckCalculatedResult(1000, Arrays.asList(buildFixedFullTypeRecord(), buildFixedFullTypeRecord()));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertFullTypeRecordsEqualsWithDifferentDecimalScale() {
        Map<String, Object> recordMap = buildFixedFullTypeRecord();
        recordMap.forEach((key, value) -> {
            if (value instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) value;
                recordMap.put(key, decimal.setScale(decimal.scale() + 1, RoundingMode.CEILING));
            }
        });
        RecordTableInventoryCheckCalculatedResult actual = new RecordTableInventoryCheckCalculatedResult(1000, Collections.singletonList(recordMap));
        RecordTableInventoryCheckCalculatedResult expected = new RecordTableInventoryCheckCalculatedResult(1000, Collections.singletonList(buildFixedFullTypeRecord()));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertNotEqualsWithDifferentRecordsCount() {
        assertThat(new RecordTableInventoryCheckCalculatedResult(1000, Collections.singletonList(buildFixedFullTypeRecord())),
                not(new RecordTableInventoryCheckCalculatedResult(1000, Collections.emptyList())));
    }
    
    @Test
    void assertNotEqualsWithDifferentMaxUniqueKeyValue() {
        assertThat(new RecordTableInventoryCheckCalculatedResult(1000, Collections.singletonList(buildFixedFullTypeRecord())),
                not(new RecordTableInventoryCheckCalculatedResult(1001, Collections.singletonList(buildFixedFullTypeRecord()))));
    }
    
    @Test
    void assertNotEqualsWithDifferentRandomColumnValue() {
        Map<String, Object> record = buildFixedFullTypeRecord();
        RecordTableInventoryCheckCalculatedResult result1 = new RecordTableInventoryCheckCalculatedResult(1000, Collections.singletonList(record));
        record.forEach((key, value) -> {
            RecordTableInventoryCheckCalculatedResult result2 = new RecordTableInventoryCheckCalculatedResult(
                    1000, Collections.singletonList(ConsistencyCheckDataBuilder.modifyColumnValueRandomly(buildFixedFullTypeRecord(), key)));
            assertThat(result1, not(result2));
        });
    }
    
    private Map<String, Object> buildFixedFullTypeRecord() {
        return ConsistencyCheckDataBuilder.buildFixedFullTypeRecord(1);
    }
    
    @Test
    void assertHashcode() {
        assertThat(new RecordTableInventoryCheckCalculatedResult(1000, Collections.emptyList()).hashCode(),
                is(new RecordTableInventoryCheckCalculatedResult(1000, Collections.emptyList()).hashCode()));
    }
}
