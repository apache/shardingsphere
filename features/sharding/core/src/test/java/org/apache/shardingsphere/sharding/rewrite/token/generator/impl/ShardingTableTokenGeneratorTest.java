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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.context.aware.CursorAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingTableToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class ShardingTableTokenGeneratorTest {
    
    @Mock
    private ShardingRule rule;
    
    private ShardingTableTokenGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new ShardingTableTokenGenerator(rule);
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithCursorAware() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class, withSettings().extraInterfaces(CursorAware.class))));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotTableAvailable() {
        generator.setRouteContext(new RouteContext());
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithAllBindingTables() {
        Collection<String> logicTableNames = Arrays.asList("foo_tbl", "bar_tbl");
        when(rule.getShardingLogicTableNames(logicTableNames)).thenReturn(logicTableNames);
        when(rule.isAllBindingTables(logicTableNames)).thenReturn(true);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(logicTableNames);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithTableSharding() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.containsTableSharding()).thenReturn(true);
        generator.setRouteContext(routeContext);
        assertTrue(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertGenerateSQLTokenWithNotTableAvailable() {
        assertTrue(generator.generateSQLTokens(mock(SQLStatementContext.class)).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokenWithTableAvailable() {
        when(rule.findShardingTable("foo_tbl")).thenReturn(Optional.of(mock()));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(Arrays.asList(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))),
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_tbl")))));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(ShardingTableToken.class));
    }
}
