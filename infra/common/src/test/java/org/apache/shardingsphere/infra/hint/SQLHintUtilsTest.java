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

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLHintUtilsTest {
    
    @Test
    void assertSQLHintWriteRouteOnlyWithCommentString() {
        assertTrue(SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */").isWriteRouteOnly());
    }
    
    @Test
    void assertSQLHintSkipSQLRewrite() {
        assertTrue(SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */").isSkipSQLRewrite());
    }
    
    @Test
    void assertSQLHintSkipMetadataValidate() {
        assertTrue(SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SKIP_METADATA_VALIDATE=true */").isSkipMetadataValidate());
    }
    
    @Test
    void assertSQLHintDisableAuditNames() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_audit1 sharding_audit2 */");
        assertThat(actual.getDisableAuditNames().size(), is(2));
        assertTrue(actual.getDisableAuditNames().containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValue() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SHARDING_DATABASE_VALUE=10 */");
        assertThat(actual.getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithTableName() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=10 */");
        assertThat(actual.getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithStringHintValue() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=a */");
        assertThat(actual.getHintShardingDatabaseValue("t_order"), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertSQLHintShardingTableValue() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SHARDING_TABLE_VALUE=10 */");
        assertThat(actual.getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithTableName() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=10 */");
        assertThat(actual.getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithStringHintValue() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=a */");
        assertThat(actual.getHintShardingTableValue("t_order"), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertSQLHintShadow() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SHADOW=true */");
        assertTrue(actual.isShadow());
    }
    
    @ParameterizedTest(name = "extractHintFormat:{0}")
    @ArgumentsSource(ExtractHintTestCaseArgumentsProvider.class)
    void assertExtractHintFormat(final String actualSQL, final boolean found) {
        HintValueContext actual = SQLHintUtils.extractHint(actualSQL);
        if (found) {
            assertTrue(actual.findHintDataSourceName().isPresent());
            assertThat(actual.findHintDataSourceName().get(), is("foo_ds"));
        } else {
            assertFalse(actual.findHintDataSourceName().isPresent());
        }
    }
    
    @ParameterizedTest(name = "extractHintFormat:{0}")
    @ArgumentsSource(RemoveHintTestCaseArgumentsProvider.class)
    void assertRemoveHint(final String actualSQL, final String expectedSQL) {
        assertThat(SQLHintUtils.removeHint(actualSQL), is(expectedSQL));
    }
    
    private static final class ExtractHintTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(Named.named("PrefixNotFound", "/* FOO_HINT: xxx=xxx */"), false),
                    Arguments.of(Named.named("ContentNotMatch", "/* SHARDINGSPHERE_HINT: xxx=xxx */"), false),
                    Arguments.of(Named.named("CommentWithoutPrefix", "SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds */"), false),
                    Arguments.of(Named.named("EmptyHintValue", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME= */"), false),
                    Arguments.of(Named.named("MalformedHintWithoutEquals", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds, DISABLE_AUDIT_NAMES */"), true),
                    Arguments.of(Named.named("EmptyDisableAuditNames", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds, DISABLE_AUDIT_NAMES= */"), true),
                    Arguments.of(Named.named("UnderlineMode", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds */"), true),
                    Arguments.of(Named.named("SpaceMode", "/* ShardingSphere hint: dataSourceName=foo_ds */"), true),
                    Arguments.of(Named.named(
                            "DBeaverHint", "/* ApplicationName=DBeaver 24.1.0 - SQLEditor <Script-84.sql> */ /* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order"), true));
        }
    }
    
    private static final class RemoveHintTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(Named.named("WithoutHint", "SELECT * FROM t_order"), "SELECT * FROM t_order"),
                    Arguments.of(Named.named("UnderlineMode", "/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order"), "SELECT * FROM t_order"),
                    Arguments.of(Named.named("SpaceMode", "/* ShardingSphere hint: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order"), "SELECT * FROM t_order"),
                    Arguments.of(Named.named("DBeaverHint", 
                                    "/* ApplicationName=DBeaver 24.1.0 - SQLEditor <Script-84.sql> */ /* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=foo_ds*/ SELECT * FROM t_order"),
                            "/* ApplicationName=DBeaver 24.1.0 - SQLEditor <Script-84.sql> */  SELECT * FROM t_order"));
        }
    }
}
