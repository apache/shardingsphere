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
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLHintUtilsTest {
    
    @Test
    void assertSQLHintWriteRouteOnlyWithCommentString() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */");
        assertTrue(actual.isWriteRouteOnly());
    }
    
    @Test
    void assertSQLHintSkipSQLRewrite() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */");
        assertTrue(actual.isSkipSQLRewrite());
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
    
    @Test
    void assertFindHintDataSourceNameExist() {
        HintValueContext actual = SQLHintUtils.extractHint("/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_1 */");
        assertTrue(actual.findHintDataSourceName().isPresent());
        assertThat(actual.findHintDataSourceName().get(), is("ds_1"));
    }
    
    @Test
    void assertFindHintDataSourceNameAliasExist() {
        HintValueContext actual = SQLHintUtils.extractHint("/* ShardingSphere hint: dataSourceName=ds_1 */");
        assertTrue(actual.findHintDataSourceName().isPresent());
        assertThat(actual.findHintDataSourceName().get(), is("ds_1"));
    }
}
