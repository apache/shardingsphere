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

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthTemporalHandlerTest {
    
    private final MonthTemporalHandler temporalHandler = new MonthTemporalHandler();
    
    @Test
    void assertParse() {
        assertThat(temporalHandler.parse("1", DateTimeFormatter.ofPattern("M")), is(Month.JANUARY));
    }
    
    @Test
    void assertConvertTo() {
        assertThat(temporalHandler.convertTo(Month.of(1)), is(Month.JANUARY));
    }
    
    @Test
    void assertIsAfter() {
        assertFalse(temporalHandler.isAfter(Month.JANUARY, Month.FEBRUARY, 1));
        assertTrue(temporalHandler.isAfter(Month.FEBRUARY, Month.JANUARY, 1));
        assertFalse(temporalHandler.isAfter(Month.OCTOBER, Month.DECEMBER, 1));
        assertTrue(temporalHandler.isAfter(Month.OCTOBER, Month.NOVEMBER, 3));
    }
    
    @Test
    void assertAdd() {
        assertThat(temporalHandler.add(Month.JANUARY, 1, ChronoUnit.MONTHS), is(Month.FEBRUARY));
    }
}
