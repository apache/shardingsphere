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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol;

import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Timestamp;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class PostgreSQLTextTimestampUtilsTest {
    
    private final String input;
    
    private final Timestamp expected;
    
    @Parameters(name = "{0}")
    public static Iterable<Object[]> textValues() {
        return Arrays.asList(
                new Object[]{"20211012 2323", Timestamp.valueOf("2021-10-12 23:23:00")},
                new Object[]{"20211012 23:23", Timestamp.valueOf("2021-10-12 23:23:00")},
                new Object[]{"20211012 232323", Timestamp.valueOf("2021-10-12 23:23:23")},
                new Object[]{"2021-10-12 23:23:23", Timestamp.valueOf("2021-10-12 23:23:23")},
                new Object[]{"2021-10-12 23:23:23.1", Timestamp.valueOf("2021-10-12 23:23:23.1")},
                new Object[]{"2021-10-12 23:23:23.12", Timestamp.valueOf("2021-10-12 23:23:23.12")},
                new Object[]{"2021-10-12 23:23:23.123", Timestamp.valueOf("2021-10-12 23:23:23.123")},
                new Object[]{"2021-10-12 23:23:23.123+08", Timestamp.valueOf("2021-10-12 23:23:23.123")},
                new Object[]{"2021-10-12T23:23:23.123+08", Timestamp.valueOf("2021-10-12 23:23:23.123")},
                new Object[]{"2021-10-12 23:23:23.12345", Timestamp.valueOf("2021-10-12 23:23:23.12345")},
                new Object[]{"2021-10-12 23:23:23.12345+0800", Timestamp.valueOf("2021-10-12 23:23:23.12345")},
                new Object[]{"20211012 23:23:23.12345+0800", Timestamp.valueOf("2021-10-12 23:23:23.12345")},
                new Object[]{"2021-10-12 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")},
                new Object[]{"211012 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")},
                new Object[]{"10/12/21 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")},
                new Object[]{"2021-10-12 23:23:23.123456", Timestamp.valueOf("2021-10-12 23:23:23.123456")},
                new Object[]{"2021-10-12 23:23:23.1234567", Timestamp.valueOf("2021-10-12 23:23:23.1234567")},
                new Object[]{"2021-10-12 23:23:23.12345678", Timestamp.valueOf("2021-10-12 23:23:23.12345678")},
                new Object[]{"2021-10-12 23:23:23.123456789", Timestamp.valueOf("2021-10-12 23:23:23.123456789")},
                new Object[]{"2021-10-12 23:23:23.123456 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456")},
                new Object[]{"2021-10-12 23:23:23.1234567 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.1234567")},
                new Object[]{"2021-10-12 23:23:23.12345678 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.12345678")},
                new Object[]{"2021-10-12 23:23:23.123456789+08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456789")},
                new Object[]{"2021-10-12 23:23:23.123456789 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456789")},
                new Object[]{"2021-10-12 23:23:23.123456 -08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456")},
                new Object[]{"2021-3-3 23:23:23.123456", Timestamp.valueOf("2021-03-03 23:23:23.123456")});
    }
    
    @Test
    public void assertGetLocalDateTimeNoExceptionOccurs() {
        assertThat(PostgreSQLTextTimestampUtils.parse(input), is(expected));
    }
}
