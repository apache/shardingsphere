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

import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.AggregationDistinctTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.AggregationDistinctToken;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AggregationDistinctTokenGeneratorTest {

    @Test
    public void assertIsGenerateSQLToken() {
        AggregationDistinctTokenGenerator aggregationDistinctTokenGenerator = new AggregationDistinctTokenGenerator();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        assertTrue(aggregationDistinctTokenGenerator.isGenerateSQLToken(selectStatementContext));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        assertFalse(aggregationDistinctTokenGenerator.isGenerateSQLToken(insertStatementContext));
    }

    @Test
    public void assertGenerateSQLToken() {
        AggregationDistinctProjection aggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        final String testAlias = "AVG_DERIVED_COUNT_0";
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(testAlias));
        final int testStartIndex = 0;
        when(aggregationDistinctProjection.getStartIndex()).thenReturn(testStartIndex);
        final int testStopIndex = 2;
        when(aggregationDistinctProjection.getStopIndex()).thenReturn(testStopIndex);
        final String testDistinctInnerExpression = "TEST_DISTINCT_INNER_EXPRESSION";
        when(aggregationDistinctProjection.getDistinctInnerExpression()).thenReturn(testDistinctInnerExpression);
        List<AggregationDistinctProjection> aggregationDistinctProjectionList = new LinkedList<>();
        aggregationDistinctProjectionList.add(aggregationDistinctProjection);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(aggregationDistinctProjectionList);
        AggregationDistinctTokenGenerator aggregationDistinctTokenGenerator = new AggregationDistinctTokenGenerator();
        List<AggregationDistinctToken> generateSQLTokensResult = aggregationDistinctTokenGenerator.generateSQLTokens(selectStatementContext).stream().collect(Collectors.toList());
        assertThat(generateSQLTokensResult.get(0).toString(), is(testDistinctInnerExpression + " AS " + testAlias));
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of("TEST_ERROR_ALIAS"));
        generateSQLTokensResult = aggregationDistinctTokenGenerator.generateSQLTokens(selectStatementContext).stream().collect(Collectors.toList());
        assertThat(generateSQLTokensResult.get(0).toString(), is(testDistinctInnerExpression));
    }
}
