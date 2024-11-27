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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class YearMonthTemporalHandlerTest {
    
    private final YearMonthTemporalHandler temporalHandler = new YearMonthTemporalHandler();
    
    @Test
    void assertParse() {
        assertThat(temporalHandler.parse("2000-01", DateTimeFormatter.ofPattern("yyyy-MM")), is(YearMonth.of(2000, 1)));
    }
    
    @Test
    void assertConvertTo() {
        assertThat(temporalHandler.convertTo(YearMonth.of(2000, 1)), is(YearMonth.of(2000, 1)));
    }
    
    @Test
    void assertIsAfter() {
        assertFalse(temporalHandler.isAfter(YearMonth.of(2000, 1), YearMonth.of(2000, 1), 1));
    }
    
    @Test
    void assertAdd() {
        assertThat(temporalHandler.add(YearMonth.of(2000, 1), 1, ChronoUnit.MONTHS), is(YearMonth.of(2000, 2)));
    }
}
