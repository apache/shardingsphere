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

package org.apache.shardingsphere.infra.util.datetime;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DateTimeFormatterFactoryTest {
    
    @Test
    void assertGetDatetimeFormatter() {
        assertThat(DateTimeFormatterFactory.getDatetimeFormatter().parse("1970-01-01 00:00:00").toString(), is("{},ISO resolved to 1970-01-01T00:00"));
    }
    
    @Test
    void assertGetDateFormatter() {
        assertThat(DateTimeFormatterFactory.getDateFormatter().parse("1970-01-01").toString(), is("{},ISO resolved to 1970-01-01"));
    }
    
    @Test
    void assertGetTimeFormatter() {
        assertThat(DateTimeFormatterFactory.getTimeFormatter().parse("00:00:00").toString(), is("{},ISO resolved to 00:00"));
    }
    
    @Test
    void assertGetShortMillisDatetimeFormatter() {
        assertThat(DateTimeFormatterFactory.getShortMillisDatetimeFormatter().parse("1970-01-01 00:00:00.0").toString(), is("{},ISO resolved to 1970-01-01T00:00"));
    }
    
    @Test
    void assertGetDoubleMillisDatetimeFormatter() {
        assertThat(DateTimeFormatterFactory.getDoubleMillisDatetimeFormatter().parse("1970-01-01 00:00:00.01").toString(), is("{},ISO resolved to 1970-01-01T00:00:00.010"));
    }
    
    @Test
    void assertGetLongMillisDatetimeFormatter() {
        assertThat(DateTimeFormatterFactory.getLongMillisDatetimeFormatter().parse("1970-01-01 00:00:00.000").toString(), is("{},ISO resolved to 1970-01-01T00:00"));
    }
    
    @Test
    void assertGetFullMillisDatetimeFormatter() {
        assertThat(DateTimeFormatterFactory.getFullMillisDatetimeFormatter().parse("1970-01-01 00:00:00.000001").toString(), is("{},ISO resolved to 1970-01-01T00:00:00.000001"));
    }
    
    @Test
    void assertGetFullTimeFormatter() {
        assertThat(DateTimeFormatterFactory.getFullTimeFormatter().parse("00:00:00.000001").toString(), is("{},ISO resolved to 00:00:00.000001"));
    }
}
