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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLHintUtilsTest {
    
    @Test
    void assertGetSQLHintPropsWithNoProp() {
        assertTrue(SQLHintUtils.getSQLHintProps("/* */").isEmpty());
    }
    
    @Test
    void assertGetSQLHintPropsWithSingleProp() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: TABLE_NAME=t_order */");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("TABLE_NAME"), is("t_order"));
    }
    
    @Test
    void assertGetSQLHintPropsWithMultiProps() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: TABLE_NAME=t_order, COLUMN_NAME=order_id */");
        assertThat(actual.size(), is(2));
        assertThat(actual.get("TABLE_NAME"), is("t_order"));
        assertThat(actual.get("COLUMN_NAME"), is("order_id"));
    }
    
    @Test
    void assertGetSQLHintPropsWithWrongFormat() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: TABLE_NAME=t_order, , DATABASE_NAME:sharding_db, COLUMN_NAME=order_id */");
        assertThat(actual.size(), is(2));
        assertThat(actual.get("TABLE_NAME"), is("t_order"));
        assertThat(actual.get("COLUMN_NAME"), is("order_id"));
    }
    
    @Test
    void assertGetSplitterSQLHintValue() {
        Collection<String> actual = SQLHintUtils.getSplitterSQLHintValue("  sharding_audit1    sharding_audit2 ");
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @Test
    void assertGetSQLHintPropsWithDataSourceName() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_0 */");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("DATA_SOURCE_NAME"), is("ds_0"));
    }
    
    @Test
    void assertGetSQLHintPropsWithDataSourceNameAlias() {
        Properties actual = SQLHintUtils.getSQLHintProps("/* ShardingSphere hint: dataSourceName=ds_0 */");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("dataSourceName"), is("ds_0"));
    }
    
    @Test
    void assertSQLHintWriteRouteOnlyWithCommentString() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().isWriteRouteOnly());
    }
    
    @Test
    void assertSQLHintSkipSQLRewrite() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().isSkipSQLRewrite());
    }
    
    @Test
    void assertSQLHintDisableAuditNames() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_audit1 sharding_audit2 */");
        assertTrue(actual.isPresent());
        Collection<String> actualDisableAuditNames = SQLHintUtils.getSplitterSQLHintValue(actual.get().getDisableAuditNames());
        assertThat(actualDisableAuditNames.size(), is(2));
        assertTrue(actualDisableAuditNames.containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValue() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SHARDING_DATABASE_VALUE=10 */");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithTableName() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=10 */");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithStringHintValue() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=a */");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintShardingDatabaseValue("t_order"), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertSQLHintShardingTableValue() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SHARDING_TABLE_VALUE=10 */");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithTableName() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=10 */");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithStringHintValue() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=a */");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintShardingTableValue("t_order"), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertSQLHintShadow() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SHADOW=true */");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().isShadow());
    }
    
    @Test
    void assertFindHintDataSourceNameExist() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_1 */");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().findHintDataSourceName().isPresent());
        assertThat(actual.get().findHintDataSourceName().get(), is("ds_1"));
    }
    
    @Test
    void assertFindHintDataSourceNameAliasExist() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* ShardingSphere hint: dataSourceName=ds_1 */");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().findHintDataSourceName().isPresent());
        assertThat(actual.get().findHintDataSourceName().get(), is("ds_1"));
    }
    
    @Test
    void assertFindHintDataSourceNameNotExist() {
        Optional<HintValueContext> actual = SQLHintUtils.extractHint("/* no hint */");
        assertFalse(actual.isPresent());
    }
}
