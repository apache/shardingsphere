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
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectionsTokenGeneratorTest {
    
    private static final String TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION = "TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION";
    
    private static final String TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS = "TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS";
    
    private static final String TEST_DERIVED_PROJECTION_ALIAS = "TEST_DERIVED_PROJECTION_ALIAS";
    
    private static final String TEST_LOGIC_TABLE_NAME = "TEST_LOGIC_TABLE_NAME";
    
    private static final String TEST_ACTUAL_TABLE_NAME_WRAPPED = "TEST_ACTUAL_TABLE_NAME_WRAPPED";
    
    private static final String TEST_OTHER_DERIVED_PROJECTION_ALIAS = "TEST_OTHER_DERIVED_PROJECTION_ALIAS";
    
    private static final String TEST_OTHER_DERIVED_PROJECTION_EXPRESSION = "TEST_OTHER_DERIVED_PROJECTION_EXPRESSION";
    
    @Mock
    private RouteUnit routeUnit;
    
    @BeforeEach
    void setup() {
        RouteMapper routeMapper = mock(RouteMapper.class);
        when(routeMapper.getLogicName()).thenReturn(TEST_LOGIC_TABLE_NAME);
        when(routeMapper.getActualName()).thenReturn("TEST_ACTUAL_TABLE_NAME");
        when(routeUnit.getTableMappers()).thenReturn(Collections.singleton(routeMapper));
    }
    
    @Test
    void assertIsGenerateInsertToken() {
        assertFalse(getProjectionsTokenGenerator().isGenerateSQLToken(mock(InsertStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSelectToken() {
        assertFalse(getProjectionsTokenGenerator().isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsGenerateSelectToken() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        AggregationProjection aggregationProjection = getAggregationProjection();
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singleton(aggregationProjection));
        assertTrue(getProjectionsTokenGenerator().isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        Collection<Projection> projections = Arrays.asList(getAggregationProjection(), getDerivedProjection(), getOtherDerivedProjection());
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(projections);
        when(selectStatementContext.getProjectionsContext().getStopIndex()).thenReturn(2);
        when(selectStatementContext.getSqlStatement()).thenReturn(new MySQLSelectStatement());
        ProjectionsTokenGenerator generator = getProjectionsTokenGenerator();
        ProjectionsToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit), is(", " + TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION + " AS " + TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS + " "
                + ", " + TEST_ACTUAL_TABLE_NAME_WRAPPED + ".null" + " AS " + TEST_DERIVED_PROJECTION_ALIAS + " "
                + ", " + TEST_OTHER_DERIVED_PROJECTION_EXPRESSION + " AS " + TEST_OTHER_DERIVED_PROJECTION_ALIAS + " "));
    }
    
    private ProjectionsTokenGenerator getProjectionsTokenGenerator() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(Collections.singleton(routeUnit));
        ProjectionsTokenGenerator result = new ProjectionsTokenGenerator();
        result.setRouteContext(routeContext);
        return result;
    }
    
    private AggregationProjection getAggregationProjection() {
        AggregationDistinctProjection derivedAggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(derivedAggregationDistinctProjection.getDistinctInnerExpression()).thenReturn(TEST_AGGREGATION_DISTINCT_PROJECTION_DISTINCT_INNER_EXPRESSION);
        when(derivedAggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue(TEST_AGGREGATION_DISTINCT_PROJECTION_ALIAS)));
        AggregationProjection result = mock(AggregationProjection.class);
        when(result.getDerivedAggregationProjections()).thenReturn(Collections.singletonList(derivedAggregationDistinctProjection));
        return result;
    }
    
    private DerivedProjection getDerivedProjection() {
        OwnerSegment ownerSegment = mock(OwnerSegment.class, RETURNS_DEEP_STUBS);
        when(ownerSegment.getIdentifier().getValue()).thenReturn(TEST_LOGIC_TABLE_NAME);
        when(ownerSegment.getIdentifier().getQuoteCharacter().wrap(anyString())).thenReturn(TEST_ACTUAL_TABLE_NAME_WRAPPED);
        ColumnOrderByItemSegment oldColumnOrderByItemSegment = mock(ColumnOrderByItemSegment.class, RETURNS_DEEP_STUBS);
        when(oldColumnOrderByItemSegment.getColumn().getOwner()).thenReturn(Optional.of(ownerSegment));
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
        when(result.getColumnName()).thenReturn(TEST_OTHER_DERIVED_PROJECTION_EXPRESSION);
        return result;
    }
}
