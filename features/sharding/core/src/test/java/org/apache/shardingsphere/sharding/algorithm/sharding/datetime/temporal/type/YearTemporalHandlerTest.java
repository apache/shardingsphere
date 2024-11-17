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

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class YearTemporalHandlerTest {
    
    private final YearTemporalHandler temporalHandler = new YearTemporalHandler();
    
    @Test
    void assertParse() {
        assertThat(temporalHandler.parse("2000", DateTimeFormatter.ofPattern("yyyy")), is(Year.of(2000)));
    }
    
    @Test
    void assertConvertTo() {
        assertThat(temporalHandler.convertTo(Year.of(2000)), is(Year.of(2000)));
    }
    
    @Test
    void assertIsAfter() {
        assertFalse(temporalHandler.isAfter(Year.of(2000), Year.of(2001), 1));
    }
    
    @Test
    void assertAdd() {
        assertThat(temporalHandler.add(Year.of(2000), 1, ChronoUnit.YEARS), is(Year.of(2001)));
    }
}
