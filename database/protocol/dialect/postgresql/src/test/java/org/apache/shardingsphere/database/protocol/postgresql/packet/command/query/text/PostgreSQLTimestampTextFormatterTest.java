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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.text;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PostgreSQLTimestampTextFormatterTest {
    
    private final PostgreSQLTimestampTextFormatter formatter = new PostgreSQLTimestampTextFormatter();
    
    @Test
    void assertFormatWithoutFractionalSeconds() {
        Timestamp timestamp = Timestamp.valueOf("1973-06-03 10:30:01.0");
        assertThat(formatter.format(timestamp), is("1973-06-03 10:30:01"));
    }
    
    @Test
    void assertFormatWithFractionalSeconds() {
        Timestamp timestamp = Timestamp.valueOf("1973-06-03 10:30:01.123");
        assertThat(formatter.format(timestamp), is("1973-06-03 10:30:01.123"));
    }
    
    @Test
    void assertFormatWithTrailingZerosInFraction() {
        Timestamp timestamp = Timestamp.valueOf("1973-06-03 10:30:01.500");
        assertThat(formatter.format(timestamp), is("1973-06-03 10:30:01.5"));
    }
    
    @Test
    void assertFormatWithMicrosecondPrecision() {
        Timestamp timestamp = Timestamp.valueOf("1973-06-03 10:30:01.123456");
        assertThat(formatter.format(timestamp), is("1973-06-03 10:30:01.123456"));
    }
    
    @Test
    void assertFormatWithLeadingZerosInFraction() {
        Timestamp timestamp = Timestamp.valueOf("1973-06-03 10:30:01.000001");
        assertThat(formatter.format(timestamp), is("1973-06-03 10:30:01.000001"));
    }
    
    @Test
    void assertFormatWithNanosecondsTruncatedToMicroseconds() {
        Timestamp timestamp = Timestamp.valueOf("1973-06-03 10:30:01.123456789");
        assertThat(formatter.format(timestamp), is("1973-06-03 10:30:01.123456"));
    }
    
    @Test
    void assertFormatWithNonTimestampValue() {
        String nonTimestamp = "test_string";
        assertThat(formatter.format(nonTimestamp), is("test_string"));
    }
}
