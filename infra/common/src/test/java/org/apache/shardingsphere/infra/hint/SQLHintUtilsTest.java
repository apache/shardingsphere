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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLHintUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("extractHintBooleanArguments")
    void assertExtractHintWithBooleanProperties(final String name, final String sql, final boolean expectedWriteRouteOnly, final boolean expectedSkipSQLRewrite,
                                                final boolean expectedSkipMetadataValidate, final boolean expectedShadow) {
        HintValueContext actual = SQLHintUtils.extractHint(sql);
        assertThat(actual.isWriteRouteOnly(), is(expectedWriteRouteOnly));
        assertThat(actual.isSkipSQLRewrite(), is(expectedSkipSQLRewrite));
        assertThat(actual.isSkipMetadataValidate(), is(expectedSkipMetadataValidate));
        assertThat(actual.isShadow(), is(expectedShadow));
    }
    
    @Test
    void assertExtractHintWithDisableAuditNames() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_audit1 sharding_audit2 */");
        assertThat(actual.getDisableAuditNames().size(), is(2));
        assertTrue(actual.getDisableAuditNames().containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("extractHintShardingDatabaseValueArguments")
    void assertExtractHintWithShardingDatabaseValue(final String name, final String sql, final Collection<Comparable<?>> expectedValues) {
        assertThat(SQLHintUtils.extractHint(sql).getHintShardingDatabaseValue("t_order"), is(expectedValues));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("extractHintShardingTableValueArguments")
    void assertExtractHintWithShardingTableValue(final String name, final String sql, final Collection<Comparable<?>> expectedValues) {
        assertThat(SQLHintUtils.extractHint(sql).getHintShardingTableValue("t_order"), is(expectedValues));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("extractHintDataSourceNameArguments")
    void assertExtractHintWithDataSourceName(final String name, final String sql, final String expectedDataSourceName) {
        HintValueContext actual = SQLHintUtils.extractHint(sql);
        assertThat(actual.findHintDataSourceName().orElse(""), is(expectedDataSourceName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("removeHintArguments")
    void assertRemoveHint(final String name, final String sql, final String expectedSql) {
        assertThat(SQLHintUtils.removeHint(sql), is(expectedSql));
    }
    
    private static Stream<Arguments> extractHintBooleanArguments() {
        return Stream.of(
                Arguments.of("write_route_only", "/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */", true, false, false, false),
                Arguments.of("skip_sql_rewrite", "/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */", false, true, false, false),
                Arguments.of("skip_metadata_validate", "/* SHARDINGSPHERE_HINT: SKIP_METADATA_VALIDATE=true */", false, false, true, false),
                Arguments.of("shadow", "/* SHARDINGSPHERE_HINT: SHADOW=true */", false, false, false, true),
                Arguments.of("unrelated_hint", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds */", false, false, false, false));
    }
    
    private static Stream<Arguments> extractHintShardingDatabaseValueArguments() {
        return Stream.of(
                Arguments.of("default_numeric_value", "/* SHARDINGSPHERE_HINT: SHARDING_DATABASE_VALUE=10 */", Collections.<Comparable<?>>singletonList(new BigInteger("10"))),
                Arguments.of("table_numeric_value", "/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=10 */", Collections.<Comparable<?>>singletonList(new BigInteger("10"))),
                Arguments.of("table_alias_numeric_value", "/* SHARDINGSPHERE_HINT: t_order.shardingDatabaseValue=10 */", Collections.<Comparable<?>>emptyList()),
                Arguments.of("table_string_value", "/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=a */", Collections.<Comparable<?>>singletonList("a")));
    }
    
    private static Stream<Arguments> extractHintShardingTableValueArguments() {
        return Stream.of(
                Arguments.of("default_numeric_value", "/* SHARDINGSPHERE_HINT: SHARDING_TABLE_VALUE=10 */", Collections.<Comparable<?>>singletonList(new BigInteger("10"))),
                Arguments.of("table_numeric_value", "/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=10 */", Collections.<Comparable<?>>singletonList(new BigInteger("10"))),
                Arguments.of("table_alias_numeric_value", "/* SHARDINGSPHERE_HINT: t_order.shardingTableValue=10 */", Collections.<Comparable<?>>emptyList()),
                Arguments.of("table_string_value", "/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=a */", Collections.<Comparable<?>>singletonList("a")));
    }
    
    private static Stream<Arguments> extractHintDataSourceNameArguments() {
        return Stream.of(
                Arguments.of("prefix_not_found", "/* FOO_HINT: xxx=xxx */", ""),
                Arguments.of("content_not_match", "/* SHARDINGSPHERE_HINT: xxx=xxx */", ""),
                Arguments.of("comment_without_prefix", "SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds */", ""),
                Arguments.of("comment_without_suffix", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds", ""),
                Arguments.of("empty_hint_value", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME= */", ""),
                Arguments.of("malformed_hint_without_equals", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds, DISABLE_AUDIT_NAMES */", "foo_ds"),
                Arguments.of("empty_disable_audit_names", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds, DISABLE_AUDIT_NAMES= */", "foo_ds"),
                Arguments.of("underline_mode", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds */", "foo_ds"),
                Arguments.of("space_mode", "/* ShardingSphere hint: dataSourceName=foo_ds */", "foo_ds"),
                Arguments.of("dbeaver_hint", "/* ApplicationName=DBeaver 24.1.0 - SQLEditor <Script-84.sql> */ /* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order", "foo_ds"));
    }
    
    private static Stream<Arguments> removeHintArguments() {
        return Stream.of(
                Arguments.of("without_hint", "SELECT * FROM t_order", "SELECT * FROM t_order"),
                Arguments.of("underline_mode", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order", "SELECT * FROM t_order"),
                Arguments.of("space_mode", "/* ShardingSphere hint: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order", "SELECT * FROM t_order"),
                Arguments.of("dbeaver_hint",
                        "/* ApplicationName=DBeaver 24.1.0 - SQLEditor <Script-84.sql> */ /* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order",
                        "/* ApplicationName=DBeaver 24.1.0 - SQLEditor <Script-84.sql> */  SELECT * FROM t_order"));
    }
}
