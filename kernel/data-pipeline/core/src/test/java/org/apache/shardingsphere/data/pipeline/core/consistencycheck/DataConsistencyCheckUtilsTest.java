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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataConsistencyCheckUtilsTest {
    
    private static final List<String> UNIQUE_KEYS_NAMES = Arrays.asList("order_id", "user_id");
    
    @Test
    void assertIsIntegerEquals() {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        String value = "123";
        Long longValue = Long.parseLong(value);
        assertTrue(DataConsistencyCheckUtils.isMatched(equalsBuilder, longValue, Integer.parseInt(value)));
        assertTrue(DataConsistencyCheckUtils.isMatched(equalsBuilder, longValue, Short.parseShort(value)));
        assertTrue(DataConsistencyCheckUtils.isMatched(equalsBuilder, longValue, Byte.parseByte(value)));
    }
    
    @Test
    void assertIsBigDecimalEquals() {
        BigDecimal one = BigDecimal.valueOf(3322L, 1);
        BigDecimal another = BigDecimal.valueOf(33220L, 2);
        assertTrue(DataConsistencyCheckUtils.isBigDecimalEquals(one, another));
    }
    
    @Test
    void assertTimestampEquals() {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        long time = System.currentTimeMillis();
        assertTrue(DataConsistencyCheckUtils.isMatched(equalsBuilder, new Timestamp(time), new Timestamp(time / 10L * 10L + 1L)));
        assertFalse(DataConsistencyCheckUtils.isMatched(equalsBuilder, new Timestamp(time), new Timestamp(time + 1000L)));
    }
    
    @Test
    void assertIsFirstUniqueKeysValueMatched() {
        Map<String, Object> record1 = ImmutableMap.of("order_id", 101, "user_id", 1, "status", "ok");
        Map<String, Object> record2 = ImmutableMap.of("order_id", 102, "user_id", 2, "status", "ok");
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        assertTrue(DataConsistencyCheckUtils.isFirstUniqueKeyValueMatched(record1, record1, UNIQUE_KEYS_NAMES.get(0), equalsBuilder));
        assertFalse(DataConsistencyCheckUtils.isFirstUniqueKeyValueMatched(record1, record2, UNIQUE_KEYS_NAMES.get(0), equalsBuilder));
    }
}
