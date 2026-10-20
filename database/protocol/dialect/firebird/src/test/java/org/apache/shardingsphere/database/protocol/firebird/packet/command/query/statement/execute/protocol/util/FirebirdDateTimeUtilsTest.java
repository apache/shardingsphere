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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.util;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdDateTimeUtilsTest {
    
    @Test
    void assertEncodeAndDecodeDateAfterFebruary() {
        LocalDateTime sourceDateTime = LocalDateTime.of(2024, 4, 15, 10, 20, 30);
        FirebirdDateTimeUtils utils = new FirebirdDateTimeUtils(sourceDateTime);
        int actualEncodedDate = utils.getEncodedDate();
        assertThat(actualEncodedDate, is(60415));
        FirebirdDateTimeUtils decoded = new FirebirdDateTimeUtils().setDate(actualEncodedDate);
        assertThat(decoded.getYear(), is(2024));
        assertThat(decoded.getMonth(), is(4));
        assertThat(decoded.getDay(), is(15));
    }
    
    @Test
    void assertEncodeAndDecodeDateBeforeMarch() {
        LocalDateTime sourceDateTime = LocalDateTime.of(2024, 2, 1, 8, 0);
        int actualEncodedDate = FirebirdDateTimeUtils.getEncodedDate(sourceDateTime);
        assertThat(actualEncodedDate, is(60341));
        FirebirdDateTimeUtils decoded = new FirebirdDateTimeUtils().setDate(actualEncodedDate);
        assertThat(decoded.getYear(), is(2024));
        assertThat(decoded.getMonth(), is(2));
        assertThat(decoded.getDay(), is(1));
    }
    
    @Test
    void assertGetEncodedTimeAndSetTime() {
        LocalDateTime sourceDateTime = LocalDateTime.of(2024, 6, 30, 1, 2, 3, 500_000_000);
        FirebirdDateTimeUtils utils = new FirebirdDateTimeUtils(sourceDateTime);
        int expectedFractions = (sourceDateTime.getNano() / FirebirdDateTimeUtils.NANOSECONDS_PER_FRACTION)
                % FirebirdDateTimeUtils.FRACTIONS_PER_SECOND;
        int expectedEncodedTime = sourceDateTime.getHour() * FirebirdDateTimeUtils.FRACTIONS_PER_HOUR
                + sourceDateTime.getMinute() * FirebirdDateTimeUtils.FRACTIONS_PER_MINUTE
                + sourceDateTime.getSecond() * FirebirdDateTimeUtils.FRACTIONS_PER_SECOND
                + expectedFractions;
        int actualEncodedTime = utils.getEncodedTime();
        assertThat(actualEncodedTime, is(expectedEncodedTime));
        FirebirdDateTimeUtils decoded = new FirebirdDateTimeUtils().setTime(actualEncodedTime);
        assertThat(decoded.getHour(), is(sourceDateTime.getHour()));
        assertThat(decoded.getMinute(), is(sourceDateTime.getMinute()));
        assertThat(decoded.getSecond(), is(sourceDateTime.getSecond()));
        assertThat(decoded.getFractions(), is(expectedFractions));
    }
    
    @Test
    void assertGetDate() {
        LocalDateTime sourceDateTime = LocalDateTime.of(2023, 7, 1, 0, 0);
        int encodedDate = FirebirdDateTimeUtils.getEncodedDate(sourceDateTime);
        Timestamp actualDate = FirebirdDateTimeUtils.getDate(encodedDate);
        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 7, 1, 0, 0);
        assertThat(actualDate.toLocalDateTime(), is(expectedDateTime));
    }
    
    @Test
    void assertGetTime() {
        assertThat(FirebirdDateTimeUtils.getTime(452967891), is(LocalTime.of(12, 34, 56, 789_100_000)));
    }
    
    @Test
    void assertGetDateTime() {
        Timestamp actualDateTime = FirebirdDateTimeUtils.getDateTime(61315, 452967890);
        assertThat(actualDateTime.toLocalDateTime(), is(LocalDateTime.of(2026, 10, 2, 12, 34, 56, 789_000_000)));
    }
    
    @Test
    void assertGetDateTimeWithOffset() {
        LocalDateTime sourceDateTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);
        int encodedDate = FirebirdDateTimeUtils.getEncodedDate(sourceDateTime);
        int encodedTime = new FirebirdDateTimeUtils(sourceDateTime).getEncodedTime();
        Timestamp actualDateTime = FirebirdDateTimeUtils.getDateTimeWithOffset(encodedDate, encodedTime, 60);
        LocalDateTime expectedDateTime = LocalDateTime.of(2025, 1, 1, sourceDateTime.getHour(), sourceDateTime.getMinute(), sourceDateTime.getSecond(), 0);
        assertThat(actualDateTime.toLocalDateTime(), is(expectedDateTime));
    }
    
    @Test
    void assertDecodeDateForJanFebBranch() {
        FirebirdDateTimeUtils decoded = new FirebirdDateTimeUtils().setDate(60369);
        assertThat(decoded.getYear(), is(2024));
        assertThat(decoded.getMonth(), is(2));
        assertThat(decoded.getDay(), is(29));
    }
}
