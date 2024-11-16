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

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ProjectionsToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingProjectionsTokenGeneratorTest {
    
    private static final String TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION = "TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION";
    
    private static final String TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS = "TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS";
    
    private static final String TEST_DERIVED_PROJECTION_ALIAS = "TEST_DERIVED_PROJECTION_ALIAS";
    
    private static final String TEST_OTHER_DERIVED_PROJECTION_ALIAS = "TEST_OTHER_DERIVED_PROJECTION_ALIAS";
    
    private static final String TEST_OTHER_DERIVED_PROJECTION_EXPRESSION = "TEST_OTHER_DERIVED_PROJECTION_EXPRESSION";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private RouteUnit routeUnit;
    
    private ShardingProjectionsTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        routeUnit = new RouteUnit(mock(RouteMapper.class), Collections.singleton(new RouteMapper("foo_tbl", "foo_tbl_0")));
        generator = createProjectionsTokenGenerator();
    }
    
    private ShardingProjectionsTokenGenerator createProjectionsTokenGenerator() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        ShardingProjectionsTokenGenerator result = new ShardingProjectionsTokenGenerator();
        result.setRouteContext(routeContext);
        return result;
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNoSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotContainsDerivedProjection() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singleton(mock()));
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithDerivedProjections() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Arrays.asList(mock(AggregationProjection.class), mock(DerivedProjection.class)));
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithDerivedAggregationProjections() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        AggregationProjection aggregationProjection = mock(AggregationProjection.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singleton(aggregationProjection));
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getDatabaseType()).thenReturn(databaseType);
        Collection<Projection> projections = Arrays.asList(getAggregationProjection(), getDerivedProjectionWithOwner(), getDerivedProjectionWithoutOwner(), getOtherDerivedProjection(), mock());
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(projections);
        when(selectStatementContext.getProjectionsContext().getStopIndex()).thenReturn(2);
        when(selectStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        ProjectionsToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit), is(", " + TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION + " AS " + TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS + " "
                + ", foo_tbl_0.foo_col" + " AS " + TEST_DERIVED_PROJECTION_ALIAS + " "
                + ", " + "null" + " AS " + TEST_DERIVED_PROJECTION_ALIAS + " "
                + ", " + TEST_OTHER_DERIVED_PROJECTION_EXPRESSION + " AS " + TEST_OTHER_DERIVED_PROJECTION_ALIAS + " "));
    }
    
    private AggregationProjection getAggregationProjection() {
        AggregationDistinctProjection derivedAggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(derivedAggregationDistinctProjection.getDistinctInnerExpression()).thenReturn(TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION);
        when(derivedAggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue(TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS)));
        AggregationProjection result = mock(AggregationProjection.class);
        when(result.getDerivedAggregationProjections()).thenReturn(Collections.singletonList(derivedAggregationDistinctProjection));
        return result;
    }
    
    private DerivedProjection getDerivedProjectionWithOwner() {
        DerivedProjection result = mock(DerivedProjection.class);
        when(result.getAlias()).thenReturn(Optional.of(new IdentifierValue(TEST_DERIVED_PROJECTION_ALIAS)));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("foo_col"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_tbl")));
        when(result.getDerivedProjectionSegment()).thenReturn(new ColumnOrderByItemSegment(columnSegment, OrderDirection.DESC, NullsOrderType.FIRST));
        return result;
    }
    
    private DerivedProjection getDerivedProjectionWithoutOwner() {
        ColumnOrderByItemSegment oldColumnOrderByItemSegment = mock(ColumnOrderByItemSegment.class, RETURNS_DEEP_STUBS);
        when(oldColumnOrderByItemSegment.getColumn().getOwner()).thenReturn(Optional.empty());
        when(oldColumnOrderByItemSegment.getOrderDirection()).thenReturn(mock(OrderDirection.class));
        when(oldColumnOrderByItemSegment.getColumn().getIdentifier()).thenReturn(mock(IdentifierValue.class));
        DerivedProjection result = mock(DerivedProjection.class);
        when(result.getAlias()).thenReturn(Optional.of(new IdentifierValue(TEST_DERIVED_PROJECTION_ALIAS)));
        when(result.getDerivedProjectionSegment()).thenReturn(oldColumnOrderByItemSegment);
        return result;
    }
    
    private DerivedProjection getOtherDerivedProjection() {
        DerivedProjection result = mock(DerivedProjection.class);
        when(result.getDerivedProjectionSegment()).thenReturn(null);
        when(result.getAlias()).thenReturn(Optional.of(new IdentifierValue(TEST_OTHER_DERIVED_PROJECTION_ALIAS)));
        when(result.getExpression()).thenReturn(TEST_OTHER_DERIVED_PROJECTION_EXPRESSION);
        return result;
    }
}
