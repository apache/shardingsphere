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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingAggregationDistinctTokenGeneratorTest {
    
    private final ShardingAggregationDistinctTokenGenerator generator = new ShardingAggregationDistinctTokenGenerator();
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyAggregationDistinctProjection() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getAggregationDistinctProjections().isEmpty()).thenReturn(true);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithAggregationDistinctProjections() {
        assertTrue(generator.isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertGenerateSQLTokenWithDerivedAlias() {
        AggregationDistinctProjection aggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue("AVG_DERIVED_COUNT_0")));
        when(aggregationDistinctProjection.getAggregationSegment()).thenReturn(mock(AggregationProjectionSegment.class));
        when(aggregationDistinctProjection.getDistinctInnerExpression()).thenReturn("TEST_DISTINCT_INNER_EXPRESSION");
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singleton(aggregationDistinctProjection));
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(selectStatementContext));
        assertThat(actual.get(0).toString(), is("TEST_DISTINCT_INNER_EXPRESSION AS AVG_DERIVED_COUNT_0"));
        verify(selectStatementContext.getProjectionsContext()).setAggregationDistinctColumnsRewritten(true);
    }
    
    @Test
    void assertGenerateSQLTokenWithExplicitDerivedAlias() {
        IdentifierValue alias = new IdentifierValue("AGGREGATION_DISTINCT_DERIVED_0");
        AggregationProjectionSegment segment = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)");
        segment.setAlias(new AliasSegment(0, 0, alias));
        AggregationDistinctProjection projection = new AggregationDistinctProjection(0, 0, AggregationType.COUNT, segment, alias, "user_id", mock(DatabaseType.class));
        SelectStatementContext context = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(context.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singleton(projection));
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(context));
        assertThat(actual.get(0).toString(), is("user_id"));
    }
    
    @Test
    void assertGenerateSQLToken() {
        AggregationDistinctProjection aggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        when(aggregationDistinctProjection.getDistinctInnerExpression()).thenReturn("TEST_DISTINCT_INNER_EXPRESSION");
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singleton(aggregationDistinctProjection));
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue("TEST_ERROR_ALIAS")));
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(selectStatementContext));
        assertThat(actual.get(0).toString(), is("TEST_DISTINCT_INNER_EXPRESSION"));
    }
}
