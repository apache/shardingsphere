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

package org.apache.shardingsphere.infra.hint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HintValueContextTest {
    
    @Test
    void assertNotFoundHintDataSourceName() {
        assertFalse(new HintValueContext().findHintDataSourceName().isPresent());
    }
    
    @Test
    void assertFindHintDataSourceName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setDataSourceName("foo_ds");
        Optional<String> actual = hintValueContext.findHintDataSourceName();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("containsHintShardingDatabaseValueArguments")
    void assertContainsHintShardingDatabaseValue(final String name, final HintValueContext hintValueContext, final String tableName, final boolean expectedContainsHintShardingDatabaseValue) {
        assertThat(hintValueContext.containsHintShardingDatabaseValue(tableName), is(expectedContainsHintShardingDatabaseValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("containsHintShardingTableValueArguments")
    void assertContainsHintShardingTableValue(final String name, final HintValueContext hintValueContext, final String tableName, final boolean expectedContainsHintShardingTableValue) {
        assertThat(hintValueContext.containsHintShardingTableValue(tableName), is(expectedContainsHintShardingTableValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("containsHintShardingValueArguments")
    void assertContainsHintShardingValue(final String name, final HintValueContext hintValueContext, final String tableName, final boolean expectedContainsHintShardingValue) {
        assertThat(hintValueContext.containsHintShardingValue(tableName), is(expectedContainsHintShardingValue));
    }
    
    @Test
    void assertGetHintShardingTableValueWithTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingTableValues().put("FOO_TABLE.SHARDING_TABLE_VALUE", "foo_value");
        Collection<Comparable<?>> actualHintShardingTableValue = hintValueContext.getHintShardingTableValue("foo_table");
        assertThat(actualHintShardingTableValue.size(), is(1));
        assertThat(actualHintShardingTableValue.iterator().next(), is("foo_value"));
    }
    
    @Test
    void assertGetHintShardingTableValueWithoutTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingTableValues().put("SHARDING_TABLE_VALUE", "bar_value");
        Collection<Comparable<?>> actualHintShardingTableValue = hintValueContext.getHintShardingTableValue("bar_table");
        assertThat(actualHintShardingTableValue.size(), is(1));
        assertThat(actualHintShardingTableValue.iterator().next(), is("bar_value"));
    }
    
    @Test
    void assertGetHintShardingDatabaseValueWithTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingDatabaseValues().put("FOO_TABLE.SHARDING_DATABASE_VALUE", "foo_value");
        Collection<Comparable<?>> actualHintShardingDatabaseValue = hintValueContext.getHintShardingDatabaseValue("foo_table");
        assertThat(actualHintShardingDatabaseValue.size(), is(1));
        assertThat(actualHintShardingDatabaseValue.iterator().next(), is("foo_value"));
    }
    
    @Test
    void assertGetHintShardingDatabaseValueWithoutTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingDatabaseValues().put("SHARDING_DATABASE_VALUE", "bar_value");
        Collection<Comparable<?>> actualHintShardingDatabaseValue = hintValueContext.getHintShardingDatabaseValue("bar_table");
        assertThat(actualHintShardingDatabaseValue.size(), is(1));
        assertThat(actualHintShardingDatabaseValue.iterator().next(), is("bar_value"));
    }
    
    private static Stream<Arguments> containsHintShardingDatabaseValueArguments() {
        return Stream.of(
                Arguments.of("table_name_key", createHintValueContextWithShardingDatabaseValue("FOO_TABLE.SHARDING_DATABASE_VALUE"), "foo_table", true),
                Arguments.of("global_key", createHintValueContextWithShardingDatabaseValue("SHARDING_DATABASE_VALUE"), "bar_table", true),
                Arguments.of("missing_key", new HintValueContext(), "bar_table", false));
    }
    
    private static Stream<Arguments> containsHintShardingTableValueArguments() {
        return Stream.of(
                Arguments.of("table_name_key", createHintValueContextWithShardingTableValue("FOO_TABLE.SHARDING_TABLE_VALUE"), "foo_table", true),
                Arguments.of("global_key", createHintValueContextWithShardingTableValue("SHARDING_TABLE_VALUE"), "bar_table", true),
                Arguments.of("missing_key", new HintValueContext(), "bar_table", false));
    }
    
    private static Stream<Arguments> containsHintShardingValueArguments() {
        return Stream.of(
                Arguments.of("database_value", createHintValueContextWithShardingDatabaseValue("FOO_TABLE.SHARDING_DATABASE_VALUE"), "foo_table", true),
                Arguments.of("table_value", createHintValueContextWithShardingTableValue("FOO_TABLE.SHARDING_TABLE_VALUE"), "foo_table", true),
                Arguments.of("missing_value", new HintValueContext(), "bar_table", false));
    }
    
    private static HintValueContext createHintValueContextWithShardingDatabaseValue(final String key) {
        HintValueContext result = new HintValueContext();
        result.getShardingDatabaseValues().put(key, "foo_value");
        return result;
    }
    
    private static HintValueContext createHintValueContextWithShardingTableValue(final String key) {
        HintValueContext result = new HintValueContext();
        result.getShardingTableValues().put(key, "foo_value");
        return result;
    }
}
