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

package org.apache.shardingsphere.infra.merge.result.impl.local;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDataQueryResultRowTest {
    
    @Test
    void assertGetCellWithZeroIndex() {
        assertThrows(IllegalArgumentException.class, () -> new LocalDataQueryResultRow().getCell(0));
    }
    
    @Test
    void assertGetCellWithOutOfIndex() {
        assertThrows(IllegalArgumentException.class, () -> new LocalDataQueryResultRow().getCell(1));
    }
    
    @Test
    void assertGetCellWithNullValue() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(null, "null");
        assertThat(actual.getCell(1), is(""));
        assertThat(actual.getCell(2), is(""));
    }
    
    @Test
    void assertGetCellWithOptional() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(Optional.empty(), Optional.of("foo"), Optional.of(1), Optional.of(PropertiesBuilder.build(new Property("foo", "bar"))));
        assertThat(actual.getCell(1), is(""));
        assertThat(actual.getCell(2), is("foo"));
        assertThat(actual.getCell(3), is("1"));
        assertThat(actual.getCell(4), is("{\"foo\":\"bar\"}"));
    }
    
    @Test
    void assertGetCellWithStringValue() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow("foo");
        assertThat(actual.getCell(1), is("foo"));
    }
    
    @Test
    void assertGetCellWithBooleanValue() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(true, Boolean.FALSE);
        assertThat(actual.getCell(1), is("true"));
        assertThat(actual.getCell(2), is("false"));
    }
    
    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    void assertGetCellWithIntegerValue() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(1, Integer.valueOf(2));
        assertThat(actual.getCell(1), is("1"));
        assertThat(actual.getCell(2), is("2"));
    }
    
    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    void assertGetCellWithLongValue() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(1L, Long.valueOf(2L));
        assertThat(actual.getCell(1), is("1"));
        assertThat(actual.getCell(2), is("2"));
    }
    
    @Test
    void assertGetCellWithLocalDateTimeValue() {
        LocalDateTime localDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(localDateTime);
        assertThat(actual.getCell(1), is(localDateTime.toString()));
    }
    
    @Test
    void assertGetCellWithEnum() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(FixtureEnum.FOO, FixtureEnum.BAR);
        assertThat(actual.getCell(1), is("FOO"));
        assertThat(actual.getCell(2), is("BAR"));
    }
    
    @Test
    void assertGetCellWithProperties() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(new Properties(), PropertiesBuilder.build(new Property("k", "v")));
        assertThat(actual.getCell(1), is(""));
        assertThat(actual.getCell(2), is("{\"k\":\"v\"}"));
    }
    
    @Test
    void assertGetCellWithMap() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(Collections.emptyMap(), Collections.singletonMap("k", "v"));
        assertThat(actual.getCell(1), is(""));
        assertThat(actual.getCell(2), is("{\"k\":\"v\"}"));
    }
    
    @Test
    void assertGetCellWithCollection() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(Collections.emptyList(), Collections.singleton("foo"));
        assertThat(actual.getCell(1), is(""));
        assertThat(actual.getCell(2), is("[\"foo\"]"));
    }
    
    @Test
    void assertGetCellWithObject() {
        LocalDataQueryResultRow actual = new LocalDataQueryResultRow(new Object());
        assertThat(actual.getCell(1), is("{}"));
    }
    
    private enum FixtureEnum {
        FOO, BAR
    }
}
