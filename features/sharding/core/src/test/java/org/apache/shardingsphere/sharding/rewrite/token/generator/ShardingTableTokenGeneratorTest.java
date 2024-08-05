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

package org.apache.shardingsphere.sharding.rewrite.token.generator;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingTableTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingTableToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingTableTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLTokenWhenConfigAllBindingTables() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        Collection<String> logicTableNames = Arrays.asList("t_order", "t_order_item");
        when(shardingRule.getShardingLogicTableNames(logicTableNames)).thenReturn(logicTableNames);
        when(shardingRule.isAllBindingTables(logicTableNames)).thenReturn(true);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(logicTableNames);
        assertTrue(new ShardingTableTokenGenerator(shardingRule).isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWhenContainsTableSharding() {
        ShardingTableTokenGenerator generator = new ShardingTableTokenGenerator(mock(ShardingRule.class));
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.containsTableSharding()).thenReturn(true);
        generator.setRouteContext(routeContext);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokenWhenSQLStatementIsTableAvailable() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findShardingTable(anyString())).thenReturn(Optional.of(mock(ShardingTable.class)));
        ShardingTableTokenGenerator generator = new ShardingTableTokenGenerator(shardingRule);
        CreateTableStatementContext sqlStatementContext = mock(CreateTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(ShardingTableToken.class));
    }
    
    @Test
    void assertGenerateSQLTokenWhenSQLStatementIsNotTableAvailable() {
        ShardingTableTokenGenerator generator = new ShardingTableTokenGenerator(mock(ShardingRule.class));
        SQLStatementContext sqlStatementContext = mock(UnknownSQLStatementContext.class);
        assertThat(generator.generateSQLTokens(sqlStatementContext), is(Collections.emptyList()));
    }
}
