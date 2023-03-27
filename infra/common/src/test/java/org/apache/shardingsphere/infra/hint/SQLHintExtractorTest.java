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

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLHintExtractorTest {
    
    @Test
    void assertSQLHintWriteRouteOnly() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */", 0, 0)));
        assertTrue(new SQLHintExtractor(statement).isHintWriteRouteOnly());
    }
    
    @Test
    void assertSQLHintWriteRouteOnlyWithCommentString() {
        assertTrue(new SQLHintExtractor("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */").isHintWriteRouteOnly());
    }
    
    @Test
    void assertSQLHintSkipSQLRewrite() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */", 0, 0)));
        assertTrue(new SQLHintExtractor(statement).isHintSkipSQLRewrite());
    }
    
    @Test
    void assertSQLHintSkipSQLRewriteWithCommentString() {
        assertTrue(new SQLHintExtractor("/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */").isHintSkipSQLRewrite());
    }
    
    @Test
    void assertSQLHintDisableAuditNames() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_audit1 sharding_audit2 */", 0, 0)));
        Collection<String> actual = new SQLHintExtractor(statement).findDisableAuditNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @Test
    void assertSQLHintDisableAuditNamesWithCommentString() {
        Collection<String> actual = new SQLHintExtractor("/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_audit1 sharding_audit2 */").findDisableAuditNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SHARDING_DATABASE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithTableName() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithCommentString() {
        assertThat(new SQLHintExtractor("/* SHARDINGSPHERE_HINT: SHARDING_DATABASE_VALUE=10 */").getHintShardingDatabaseValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingDatabaseValueWithStringHintValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=a */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingDatabaseValue("t_order"), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertSQLHintShardingTableValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SHARDING_TABLE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithCommentString() {
        assertThat(new SQLHintExtractor("/* SHARDINGSPHERE_HINT: SHARDING_TABLE_VALUE=10 */").getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithTableName() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingTableValue("t_order"), is(Collections.singletonList(new BigInteger("10"))));
    }
    
    @Test
    void assertSQLHintShardingTableValueWithStringHintValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=a */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingTableValue("t_order"), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertSQLHintShadow() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SHADOW=true */", 0, 0)));
        assertTrue(new SQLHintExtractor(statement).isShadow());
    }
    
    @Test
    void assertSQLHintShadowWithCommentString() {
        assertTrue(new SQLHintExtractor("/* SHARDINGSPHERE_HINT: SHADOW=true */").isShadow());
    }
    
    @Test
    void assertFindHintDataSourceNameExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_1 */", 0, 0)));
        Optional<String> dataSourceName = new SQLHintExtractor(statement).findHintDataSourceName();
        assertTrue(dataSourceName.isPresent());
        assertThat(dataSourceName.get(), is("ds_1"));
    }
    
    @Test
    void assertFindHintDataSourceNameAliasExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* ShardingSphere hint: dataSourceName=ds_1 */", 0, 0)));
        Optional<String> dataSourceName = new SQLHintExtractor(statement).findHintDataSourceName();
        assertTrue(dataSourceName.isPresent());
        assertThat(dataSourceName.get(), is("ds_1"));
    }
    
    @Test
    void assertFindHintDataSourceNameNotExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* no hint */", 0, 0)));
        Optional<String> dataSourceName = new SQLHintExtractor(statement).findHintDataSourceName();
        assertFalse(dataSourceName.isPresent());
    }
    
    @Test
    void assertFindHintDataSourceNameNotExistWithoutComment() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        Optional<String> dataSourceName = new SQLHintExtractor(statement).findHintDataSourceName();
        assertFalse(dataSourceName.isPresent());
    }
}
