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

import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ProjectionsTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ProjectionsToken;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProjectionsTokenGeneratorTest {

    private static final String TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION = "TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION";

    private static final String TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS = "TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS";

    private static final String TEST_DERIVED_PROJECTION_ALIAS = "TEST_DERIVED_PROJECTION_ALIAS";

    private static final int TEST_STOP_INDEX = 2;

    private static final String TEST_LOGIC_TABLE_NAME = "TEST_LOGIC_TABLE_NAME";

    private static final String TEST_ACTUAL_TABLE_NAME = "TEST_ACTUAL_TABLE_NAME";

    private static final String TEST_ACTUAL_TABLE_NAME_WRAPPED = "TEST_ACTUAL_TABLE_NAME_WRAPPED";

    private static final String TEST_OTHER_DERIVED_PROJECTION_ALIAS = "TEST_OTHER_DERIVED_PROJECTION_ALIAS";

    private static final String TEST_OTHER_DERIVED_PROJECTION_EXPRESSION = "TEST_OTHER_DERIVED_PROJECTION_EXPRESSION";

    private RouteUnit routeUnit = mock(RouteUnit.class);

    @Before
    public void setup() {
        RouteMapper routeMapper = mock(RouteMapper.class);
        when(routeMapper.getLogicName()).thenReturn(TEST_LOGIC_TABLE_NAME);
        when(routeMapper.getActualName()).thenReturn(TEST_ACTUAL_TABLE_NAME);
        Collection<RouteMapper> routeMapperCollection = new LinkedList<>();
        routeMapperCollection.add(routeMapper);
        when(routeUnit.getTableMappers()).thenReturn(routeMapperCollection);
    }

    @Test
    public void assertIsGenerateSQLToken() {
        ProjectionsTokenGenerator projectionsTokenGenerator = getProjectionsTokenGenerator();
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        assertFalse(projectionsTokenGenerator.isGenerateSQLToken(insertStatementContext));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.emptyList());
        assertFalse(projectionsTokenGenerator.isGenerateSQLToken(selectStatementContext));
        AggregationProjection aggregationProjection = getAggregationProjection();
        Collection<Projection> projectionCollection = new LinkedList<>();
        projectionCollection.add(aggregationProjection);
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(projectionCollection);
        assertTrue(projectionsTokenGenerator.isGenerateSQLToken(selectStatementContext));
    }

    @Test
    public void assertGenerateSQLToken() {
        Collection<Projection> projectionCollection = new LinkedList<>();
        AggregationProjection aggregationProjection = getAggregationProjection();
        projectionCollection.add(aggregationProjection);
        DerivedProjection derivedProjection = getDerivedProjection();
        projectionCollection.add(derivedProjection);
        DerivedProjection otherDerivedProjection = getOtherDerivedProjection();
        projectionCollection.add(otherDerivedProjection);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(projectionCollection);
        when(selectStatementContext.getProjectionsContext().getStopIndex()).thenReturn(TEST_STOP_INDEX);
        when(selectStatementContext.getSqlStatement()).thenReturn(new MySQLSelectStatement());
        ProjectionsTokenGenerator projectionsTokenGenerator = getProjectionsTokenGenerator();
        ProjectionsToken projectionsToken = projectionsTokenGenerator.generateSQLToken(selectStatementContext);
        assertThat(projectionsToken.toString(routeUnit), is(", " + TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION + " AS " + TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS + " "
                + ", " + TEST_ACTUAL_TABLE_NAME_WRAPPED + ".null" + " AS " + TEST_DERIVED_PROJECTION_ALIAS + " "
                + ", " + TEST_OTHER_DERIVED_PROJECTION_EXPRESSION + " AS " + TEST_OTHER_DERIVED_PROJECTION_ALIAS + " "));
    }

    private ProjectionsTokenGenerator getProjectionsTokenGenerator() {
        Collection<RouteUnit> routeUnitCollection = new LinkedList<>();
        routeUnitCollection.add(routeUnit);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(routeUnitCollection);
        ProjectionsTokenGenerator result = new ProjectionsTokenGenerator();
        result.setRouteContext(routeContext);
        return result;
    }

    private AggregationProjection getAggregationProjection() {
        AggregationDistinctProjection derivedAggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(derivedAggregationDistinctProjection.getDistinctInnerExpression()).thenReturn(TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION);
        when(derivedAggregationDistinctProjection.getAlias()).thenReturn(Optional.of(TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS));
        List<AggregationProjection> derivedAggregationProjectionList = new LinkedList<>();
        derivedAggregationProjectionList.add(derivedAggregationDistinctProjection);
        AggregationProjection aggregationProjection = mock(AggregationProjection.class);
        when(aggregationProjection.getDerivedAggregationProjections()).thenReturn(derivedAggregationProjectionList);
        return aggregationProjection;
    }

    private DerivedProjection getDerivedProjection() {
        OwnerSegment ownerSegment = mock(OwnerSegment.class, RETURNS_DEEP_STUBS);
        when(ownerSegment.getIdentifier().getValue()).thenReturn(TEST_LOGIC_TABLE_NAME);
        when(ownerSegment.getIdentifier().getQuoteCharacter().wrap(anyString())).thenReturn(TEST_ACTUAL_TABLE_NAME_WRAPPED);
        ColumnOrderByItemSegment oldColumnOrderByItemSegment = mock(ColumnOrderByItemSegment.class, RETURNS_DEEP_STUBS);
        when(oldColumnOrderByItemSegment.getColumn().getOwner()).thenReturn(Optional.of(ownerSegment));
        OrderDirection orderDirection = mock(OrderDirection.class);
        when(oldColumnOrderByItemSegment.getOrderDirection()).thenReturn(orderDirection);
        IdentifierValue oldColumnIdentifierValue = mock(IdentifierValue.class);
        when(oldColumnOrderByItemSegment.getColumn().getIdentifier()).thenReturn(oldColumnIdentifierValue);
        DerivedProjection derivedProjection = mock(DerivedProjection.class);
        when(derivedProjection.getAlias()).thenReturn(Optional.of(TEST_DERIVED_PROJECTION_ALIAS));
        when(derivedProjection.getDerivedProjection()).thenReturn(oldColumnOrderByItemSegment);
        return derivedProjection;
    }

    private DerivedProjection getOtherDerivedProjection() {
        DerivedProjection result = mock(DerivedProjection.class);
        when(result.getDerivedProjection()).thenReturn(null);
        when(result.getAlias()).thenReturn(Optional.of(TEST_OTHER_DERIVED_PROJECTION_ALIAS));
        when(result.getExpression()).thenReturn(TEST_OTHER_DERIVED_PROJECTION_EXPRESSION);
        return result;
    }
}
