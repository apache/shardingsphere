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
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLHintExtractorTest {
    
    @Test
    public void assertSQLHintWriteRouteOnly() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */", 0, 0)));
        assertTrue(new SQLHintExtractor(statement).isHintWriteRouteOnly());
    }
    
    @Test
    public void assertSQLHintSkipEncryptRewrite() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SKIP_ENCRYPT_REWRITE=true */", 0, 0)));
        assertTrue(new SQLHintExtractor(statement).isHintSkipEncryptRewrite());
    }
    
    @Test
    public void assertSQLHintDisableAuditNames() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_audit1 sharding_audit2 */", 0, 0)));
        Collection<String> actual = new SQLHintExtractor(statement).findDisableAuditNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsAll(Arrays.asList("sharding_audit1", "sharding_audit2")));
    }
    
    @Test
    public void assertSQLHintShardingDatabaseValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SHARDING_DATABASE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingDatabaseValue("t_order"), is(new BigInteger("10")));
    }
    
    @Test
    public void assertSQLHintShardingDatabaseValueWithTableName() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingDatabaseValue("t_order"), is(new BigInteger("10")));
    }
    
    @Test
    public void assertSQLHintShardingDatabaseValueWithStringHintValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=a */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingDatabaseValue("t_order"), is("a"));
    }
    
    @Test
    public void assertSQLHintShardingTableValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: SHARDING_TABLE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingTableValue("t_order"), is(new BigInteger("10")));
    }
    
    @Test
    public void assertSQLHintShardingTableValueWithTableName() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=10 */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingTableValue("t_order"), is(new BigInteger("10")));
    }
    
    @Test
    public void assertSQLHintShardingTableValueWithStringHintValue() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* SHARDINGSPHERE_HINT: t_order.SHARDING_TABLE_VALUE=a */", 0, 0)));
        assertThat(new SQLHintExtractor(statement).getHintShardingTableValue("t_order"), is("a"));
    }
}
