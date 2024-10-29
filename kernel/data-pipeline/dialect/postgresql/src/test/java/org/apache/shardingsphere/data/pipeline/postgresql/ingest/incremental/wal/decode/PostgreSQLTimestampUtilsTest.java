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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.jdbc.TimestampUtils;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLTimestampUtilsTest {
    
    @Mock
    private TimestampUtils timestampUtils;
    
    private BaseTimestampUtils baseTimestampUtils;
    
    @BeforeEach
    void setUp() {
        baseTimestampUtils = new PostgreSQLTimestampUtils(timestampUtils);
    }
    
    @Test
    void assertToTime() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        Time time = mock(Time.class);
        when(timestampUtils.toTime(calendar, "2000-01-01 00:00:00")).thenReturn(time);
        assertThat(baseTimestampUtils.toTime(calendar, "2000-01-01 00:00:00"), is(time));
    }
    
    @Test
    void assertToTimestamp() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        Timestamp timestamp = mock(Timestamp.class);
        when(timestampUtils.toTimestamp(calendar, "2000-01-01 00:00:00")).thenReturn(timestamp);
        assertThat(baseTimestampUtils.toTimestamp(calendar, "2000-01-01 00:00:00"), is(timestamp));
    }
}
