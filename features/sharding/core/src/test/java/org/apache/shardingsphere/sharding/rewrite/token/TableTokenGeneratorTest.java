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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.TableTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.TableToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
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

class TableTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLTokenWhenConfigAllBindingTables() {
        TableTokenGenerator generator = new TableTokenGenerator();
        ShardingRule shardingRule = mock(ShardingRule.class);
        Collection<String> logicTableNames = Arrays.asList("t_order", "t_order_item");
        when(shardingRule.getShardingLogicTableNames(logicTableNames)).thenReturn(logicTableNames);
        when(shardingRule.isAllBindingTables(logicTableNames)).thenReturn(true);
        generator.setShardingRule(shardingRule);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(logicTableNames);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWhenContainsTableSharding() {
        TableTokenGenerator generator = new TableTokenGenerator();
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.containsTableSharding()).thenReturn(true);
        generator.setShardingRule(mock(ShardingRule.class));
        generator.setRouteContext(routeContext);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokenWhenSQLStatementIsTableAvailable() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findTableRule(anyString())).thenReturn(Optional.of(mock(TableRule.class)));
        TableTokenGenerator generator = new TableTokenGenerator();
        generator.setShardingRule(shardingRule);
        CreateTableStatementContext sqlStatementContext = mock(CreateTableStatementContext.class);
        when(sqlStatementContext.getAllTables()).thenReturn(Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(TableToken.class));
    }
    
    @Test
    void assertGenerateSQLTokenWhenSQLStatementIsNotTableAvailable() {
        TableTokenGenerator generator = new TableTokenGenerator();
        SQLStatementContext sqlStatementContext = mock(CreateDatabaseStatementContext.class);
        assertThat(generator.generateSQLTokens(sqlStatementContext), is(Collections.emptyList()));
    }
}
