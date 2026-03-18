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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal;

import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTimeTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalTimeTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.MonthTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearMonthTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearTemporalHandler;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class TemporalHandlerFactoryTest {
    
    @Test
    void assertNewInstanceWithLocalDate() {
        assertThat(TemporalHandlerFactory.newInstance(LocalDate.now()), isA(LocalDateTemporalHandler.class));
    }
    
    @Test
    void assertNewInstanceWithLocalTime() {
        assertThat(TemporalHandlerFactory.newInstance(LocalTime.now()), isA(LocalTimeTemporalHandler.class));
    }
    
    @Test
    void assertNewInstanceWithLocalDateTime() {
        assertThat(TemporalHandlerFactory.newInstance(LocalDateTime.now()), isA(LocalDateTimeTemporalHandler.class));
    }
    
    @Test
    void assertNewInstanceWithYearMonth() {
        assertThat(TemporalHandlerFactory.newInstance(YearMonth.now()), isA(YearMonthTemporalHandler.class));
    }
    
    @Test
    void assertNewInstanceWithYear() {
        assertThat(TemporalHandlerFactory.newInstance(Year.now()), isA(YearTemporalHandler.class));
    }
    
    @Test
    void assertNewInstanceWithMonth() {
        assertThat(TemporalHandlerFactory.newInstance(Month.JANUARY), isA(MonthTemporalHandler.class));
    }
}
