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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ProjectionsToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingProjectionsTokenGeneratorTest {
    
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
        Collection<Projection> projections = Arrays.asList(
                createAggregationProjection(), createDerivedProjectionWithOwner(), createDerivedProjectionWithoutOwner(), createOtherDerivedProjection(), mock());
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(projections);
        when(selectStatementContext.getProjectionsContext().getStopIndex()).thenReturn(2);
        when(selectStatementContext.getSqlStatement()).thenReturn(new SelectStatement(databaseType));
        ProjectionsToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit),
                is(", foo_agg_expr AS foo_agg_alias , foo_tbl_0.foo_derived_col AS foo_derived_alias , bar_derived_col AS bar_derived_alias , other_expr AS other_alias "));
    }
    
    private AggregationProjection createAggregationProjection() {
        AggregationDistinctProjection derivedProjection = new AggregationDistinctProjection(0, 0, AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, ""),
                new IdentifierValue("foo_agg_alias"), "foo_agg_expr", databaseType);
        AggregationProjection result = new AggregationDistinctProjection(0, 0, AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, ""), null, "", databaseType);
        result.getDerivedAggregationProjections().add(derivedProjection);
        return result;
    }
    
    private DerivedProjection createDerivedProjectionWithOwner() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("foo_derived_col"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_tbl")));
        return new DerivedProjection("", new IdentifierValue("foo_derived_alias"), new ColumnOrderByItemSegment(columnSegment, OrderDirection.DESC, NullsOrderType.FIRST));
    }
    
    private DerivedProjection createDerivedProjectionWithoutOwner() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("bar_derived_col"));
        return new DerivedProjection("", new IdentifierValue("bar_derived_alias"), new ColumnOrderByItemSegment(columnSegment, OrderDirection.DESC, NullsOrderType.FIRST));
    }
    
    private DerivedProjection createOtherDerivedProjection() {
        return new DerivedProjection("other_expr", new IdentifierValue("other_alias"), mock());
    }
}
