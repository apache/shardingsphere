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

import java.time.LocalTime;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class PostgreSQLTextTimeUtilsTest {
    
    private final String input;
    
    private final LocalTime expected;
    
    @Parameters(name = "{0}")
    public static Iterable<Object[]> textValues() {
        return Arrays.asList(
                new Object[]{"2323", LocalTime.of(23, 23, 0)},
                new Object[]{"23:23", LocalTime.of(23, 23, 0)},
                new Object[]{"232323", LocalTime.of(23, 23, 23)},
                new Object[]{"23:23:23", LocalTime.of(23, 23, 23)},
                new Object[]{"23:23:23.1", LocalTime.of(23, 23, 23, 100_000_000)},
                new Object[]{"23:23:23.12", LocalTime.of(23, 23, 23, 120_000_000)},
                new Object[]{"23:23:23.123", LocalTime.of(23, 23, 23, 123_000_000)},
                new Object[]{"23:23:23.123+08", LocalTime.of(23, 23, 23, 123_000_000)},
                new Object[]{"23:23:23.123+08", LocalTime.of(23, 23, 23, 123_000_000)},
                new Object[]{"23:23:23.12345", LocalTime.of(23, 23, 23, 123_450_000)},
                new Object[]{"23:23:23.12345+0800", LocalTime.of(23, 23, 23, 123_450_000)},
                new Object[]{"23:23:23.12345+0800", LocalTime.of(23, 23, 23, 123_450_000)},
                new Object[]{"23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)},
                new Object[]{"23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)},
                new Object[]{"23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)},
                new Object[]{"23:23:23.123456", LocalTime.of(23, 23, 23, 123_456_000)},
                new Object[]{"23:23:23.1234567", LocalTime.of(23, 23, 23, 123_456_700)},
                new Object[]{"23:23:23.12345678", LocalTime.of(23, 23, 23, 123_456_780)},
                new Object[]{"23:23:23.123456789", LocalTime.of(23, 23, 23, 123_456_789)},
                new Object[]{"23:23:23.123456 +08:00", LocalTime.of(23, 23, 23, 123_456_000)},
                new Object[]{"23:23:23.123456 -08:00", LocalTime.of(23, 23, 23, 123_456_000)},
                new Object[]{"23:23:23.123456789 +08:00", LocalTime.of(23, 23, 23, 123_456_789)},
                new Object[]{"23:23:23.123456", LocalTime.of(23, 23, 23, 123_456_000)});
    }
    
    @Test
    public void assertParse() {
        assertThat(PostgreSQLTextTimeUtils.parse(input), is(expected));
    }
}
