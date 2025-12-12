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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PostgreSQLDateValueParserTest {
    
    @Test
    void assertParse() {
        assertThat(new PostgreSQLDateValueParser().parse("2020-01-01"), is(Date.valueOf(LocalDate.of(2020, 1, 1))));
        assertThat(new PostgreSQLDateValueParser().parse("2020-01-01 +08"), is(Date.valueOf(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.of("+8")).toLocalDate())));
        assertThat(new PostgreSQLDateValueParser().parse("2020-01-01 +08:00"), is(Date.valueOf(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.of("+8")).toLocalDate())));
        assertThat(new PostgreSQLDateValueParser().parse("2020-01-01 +08:00:00"), is(Date.valueOf(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.of("+8")).toLocalDate())));
    }
}
