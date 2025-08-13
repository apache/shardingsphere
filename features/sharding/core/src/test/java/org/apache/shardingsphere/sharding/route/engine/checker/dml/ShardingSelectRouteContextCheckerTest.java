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

package org.apache.shardingsphere.sharding.route.engine.checker.dml;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.syntax.SelectMultipleDataSourcesWithCombineException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingSelectRouteContextCheckerTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private QueryContext queryContext;
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Test
    void assertCombineExistsAndRoutesToMultipleDataSources() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        CombineSegment combineSegment = mock(CombineSegment.class);
        when(combineSegment.getCombineType()).thenReturn(CombineType.UNION_ALL);
        when(selectStatement.getCombine()).thenReturn(Optional.of(combineSegment));
        SelectStatementContext selectContext = mock(SelectStatementContext.class);
        when(selectContext.getSqlStatement()).thenReturn(selectStatement);
        when(queryContext.getSqlStatementContext()).thenReturn(selectContext);
        when(routeContext.getRouteUnits()).thenReturn(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds2")));
        assertThrows(SelectMultipleDataSourcesWithCombineException.class, () -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertCombineExistsAndRoutesToSingleDataSource() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getCombine()).thenReturn(Optional.of(mock(CombineSegment.class)));
        SelectStatementContext selectContext = mock(SelectStatementContext.class);
        when(selectContext.getSqlStatement()).thenReturn(selectStatement);
        when(queryContext.getSqlStatementContext()).thenReturn(selectContext);
        when(routeContext.getRouteUnits()).thenReturn(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds1")));
        assertDoesNotThrow(() -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertNoCombineExists() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getCombine()).thenReturn(Optional.empty());
        SelectStatementContext selectContext = mock(SelectStatementContext.class);
        when(selectContext.getSqlStatement()).thenReturn(selectStatement);
        when(queryContext.getSqlStatementContext()).thenReturn(selectContext);
        assertDoesNotThrow(() -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    private RouteUnit createMockRouteUnit(final String dataSourceName) {
        return new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.emptyList());
    }
}
