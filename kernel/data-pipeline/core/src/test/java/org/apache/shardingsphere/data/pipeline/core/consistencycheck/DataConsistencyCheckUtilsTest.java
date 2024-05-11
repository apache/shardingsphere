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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DataConsistencyCheckUtilsTest {
    
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
}
