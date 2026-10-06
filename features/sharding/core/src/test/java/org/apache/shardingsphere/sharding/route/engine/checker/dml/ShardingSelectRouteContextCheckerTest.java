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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.syntax.SelectMultipleDataSourcesWithCombineException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
        DatabaseType fixtureDatabaseType = createDatabaseType("FIXTURE");
        when(selectStatement.getDatabaseType()).thenReturn(fixtureDatabaseType);
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
        DatabaseType fixtureDatabaseType = createDatabaseType("FIXTURE");
        when(selectStatement.getDatabaseType()).thenReturn(fixtureDatabaseType);
        SelectStatementContext selectContext = mock(SelectStatementContext.class);
        when(selectContext.getSqlStatement()).thenReturn(selectStatement);
        when(queryContext.getSqlStatementContext()).thenReturn(selectContext);
        assertDoesNotThrow(() -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertOpenGaussCubeWithMultipleRouteUnits() {
        GroupBySegment groupBy = new GroupBySegment(0, 0,
                Collections.singleton(new ExpressionOrderByItemSegment(0, 0, "CUBE (user_id)", OrderDirection.ASC, null)), false, true);
        mockQueryContext(createSelectStatement("openGauss", new ProjectionsSegment(0, 0), groupBy, null));
        when(routeContext.getRouteUnits()).thenReturn(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds1")));
        UnsupportedSQLOperationException ex = assertThrows(UnsupportedSQLOperationException.class,
                () -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
        assertThat(ex.getMessage(), is("Unsupported SQL operation: openGauss CUBE query across multiple data nodes."));
    }
    
    @Test
    void assertOpenGaussAggregationWithNamedWindowAndMultipleRouteUnits() {
        AggregationProjectionSegment aggregationProjection = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(order_id) OVER window1");
        WindowItemSegment window = new WindowItemSegment(0, 0);
        window.setWindowName(new IdentifierValue("window1"));
        aggregationProjection.setWindow(window);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(aggregationProjection);
        mockQueryContext(createSelectStatement("openGauss", projections, null, null));
        when(routeContext.getRouteUnits()).thenReturn(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds1")));
        UnsupportedSQLOperationException ex = assertThrows(UnsupportedSQLOperationException.class,
                () -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
        assertThat(ex.getMessage(), is("Unsupported SQL operation: openGauss aggregate query with a named window across multiple data nodes."));
    }
    
    @Test
    void assertOpenGaussHavingWithMultipleRouteUnits() {
        HavingSegment having = new HavingSegment(0, 0, mock(ExpressionSegment.class));
        mockQueryContext(createSelectStatement("openGauss", new ProjectionsSegment(0, 0), null, having));
        when(routeContext.getRouteUnits()).thenReturn(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds1")));
        UnsupportedSQLOperationException ex = assertThrows(UnsupportedSQLOperationException.class,
                () -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
        assertThat(ex.getMessage(), is("Unsupported SQL operation: openGauss HAVING query across multiple data nodes without SQL Federation."));
    }
    
    @Test
    void assertOpenGaussCubeWithSingleRouteUnit() {
        GroupBySegment groupBy = new GroupBySegment(0, 0,
                Collections.singleton(new ExpressionOrderByItemSegment(0, 0, "CUBE(user_id)", OrderDirection.ASC, null)), false, true);
        mockQueryContext(createSelectStatement("openGauss", new ProjectionsSegment(0, 0), groupBy, null));
        when(routeContext.getRouteUnits()).thenReturn(Collections.singleton(createMockRouteUnit("ds1")));
        assertDoesNotThrow(() -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertCubeWithMultipleRouteUnitsForOtherDatabase() {
        GroupBySegment groupBy = new GroupBySegment(0, 0,
                Collections.singleton(new ExpressionOrderByItemSegment(0, 0, "CUBE(user_id)", OrderDirection.ASC, null)), false, true);
        mockQueryContext(createSelectStatement("FIXTURE", new ProjectionsSegment(0, 0), groupBy, null));
        RouteContext multiRouteContext = new RouteContext();
        multiRouteContext.getRouteUnits().addAll(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds2")));
        assertDoesNotThrow(() -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), multiRouteContext));
    }
    
    @Test
    void assertOpenGaussSelectWithoutUnsupportedFeatureWithMultipleRouteUnits() {
        mockQueryContext(createSelectStatement("openGauss", new ProjectionsSegment(0, 0), null, null));
        when(routeContext.getRouteUnits()).thenReturn(Arrays.asList(createMockRouteUnit("ds1"), createMockRouteUnit("ds1")));
        assertDoesNotThrow(() -> new ShardingSelectRouteContextChecker().check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    private void mockQueryContext(final SelectStatement selectStatement) {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
    }
    
    private SelectStatement createSelectStatement(final String databaseTypeName, final ProjectionsSegment projections, final GroupBySegment groupBy, final HavingSegment having) {
        return SelectStatement.builder().databaseType(createDatabaseType(databaseTypeName)).projections(projections).groupBy(groupBy).having(having).build();
    }
    
    private DatabaseType createDatabaseType(final String type) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getType()).thenReturn(type);
        return result;
    }
    
    private RouteUnit createMockRouteUnit(final String dataSourceName) {
        return new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.emptyList());
    }
}
