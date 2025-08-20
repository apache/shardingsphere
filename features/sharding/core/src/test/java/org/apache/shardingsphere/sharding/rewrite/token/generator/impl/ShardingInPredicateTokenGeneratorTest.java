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

import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ShardingInPredicateTokenGeneratorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private SelectStatementContext selectStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ShardingTable shardingTable;
    
    @Mock
    private StandardShardingStrategyConfiguration strategyConfiguration;
    
    @Mock
    private StandardShardingAlgorithm<Comparable<?>> standardAlgorithm;
    
    private ShardingInPredicateTokenGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new ShardingInPredicateTokenGenerator(shardingRule);
        generator.setRouteContext(routeContext);
        generator.setParameters(Arrays.asList(1L, 2L, 3L, 4L, 5L));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithValidInExpression() {
        Mockito.when(selectStatementContext.getWhereSegments()).thenReturn(createWhereSegments());
        Mockito.when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        Mockito.when(tablesContext.getTableNames()).thenReturn(Collections.singleton("t_order"));
        Mockito.when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        Mockito.when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.of(shardingTable));
        Mockito.when(shardingRule.getTableShardingStrategyConfiguration(shardingTable)).thenReturn(strategyConfiguration);
        Mockito.when(strategyConfiguration.getShardingColumn()).thenReturn("order_id");
        
        boolean result = generator.isGenerateSQLToken(selectStatementContext);
        
        Assertions.assertTrue(result);
    }
    
    @Test
    void assertGenerateSQLTokens() {
        Mockito.when(selectStatementContext.getWhereSegments()).thenReturn(createWhereSegments());
        Mockito.when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        Mockito.when(tablesContext.getTableNames()).thenReturn(Collections.singleton("t_order"));
        Mockito.when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        Mockito.when(shardingRule.findShardingTable("t_order")).thenReturn(Optional.of(shardingTable));
        Mockito.when(shardingRule.getTableShardingStrategyConfiguration(shardingTable)).thenReturn(strategyConfiguration);
        Mockito.when(strategyConfiguration.getShardingColumn()).thenReturn("order_id");
        Mockito.when(strategyConfiguration.getShardingAlgorithmName()).thenReturn("test_algorithm");
        
        Map<String, ShardingAlgorithm> algorithms = new HashMap<>();
        algorithms.put("test_algorithm", standardAlgorithm);
        Mockito.when(shardingRule.getShardingAlgorithms()).thenReturn(algorithms);
        
        Mockito.when(shardingTable.getActualDataNodes()).thenReturn(createDataNodes());
        
        Mockito.when(routeContext.getRouteUnits()).thenReturn(createRouteUnits());
        
        Collection<SQLToken> tokens = generator.generateSQLTokens(selectStatementContext);
        
        MatcherAssert.assertThat(tokens.size(), CoreMatchers.is(1));
        SQLToken token = tokens.iterator().next();
        Assertions.assertInstanceOf(ShardingInPredicateToken.class, token);
    }
    
    private Collection<WhereSegment> createWhereSegments() {
        ColumnSegment columnSegment = new ColumnSegment(40, 47, new IdentifierValue("order_id"));
        
        List<LiteralExpressionSegment> literals = Arrays.asList(
                new LiteralExpressionSegment(60, 60, 1L),
                new LiteralExpressionSegment(63, 63, 2L),
                new LiteralExpressionSegment(66, 66, 3L));
        
        ListExpression listExpression = createListExpression(58, 68, literals);
        InExpression inExpression = new InExpression(40, 80, columnSegment, listExpression, false);
        WhereSegment whereSegment = new WhereSegment(35, 85, inExpression);
        
        return Collections.singletonList(whereSegment);
    }
    
    private ListExpression createListExpression(final int startIndex, final int stopIndex, final List<LiteralExpressionSegment> literals) {
        ListExpression listExpression = new ListExpression(startIndex, stopIndex);
        
        Field itemsField = null;
        try {
            itemsField = ListExpression.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            itemsField.set(listExpression, literals);
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
        
        return listExpression;
    }
    
    private List<DataNode> createDataNodes() {
        return Arrays.asList(
                new DataNode("ds_0", "t_order_0"),
                new DataNode("ds_0", "t_order_1"));
    }
    
    private Collection<RouteUnit> createRouteUnits() {
        RouteMapper dataSourceMapper = new RouteMapper("ds", "ds_0");
        RouteMapper tableMapper1 = new RouteMapper("t_order", "t_order_0");
        RouteMapper tableMapper2 = new RouteMapper("t_order", "t_order_1");
        
        return Arrays.asList(
                new RouteUnit(dataSourceMapper, Collections.singletonList(tableMapper1)),
                new RouteUnit(dataSourceMapper, Collections.singletonList(tableMapper2)));
    }
}
