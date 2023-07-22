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
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.AggregationDistinctTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregationDistinctTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLToken() {
        AggregationDistinctTokenGenerator aggregationDistinctTokenGenerator = new AggregationDistinctTokenGenerator();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        assertTrue(aggregationDistinctTokenGenerator.isGenerateSQLToken(selectStatementContext));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        assertFalse(aggregationDistinctTokenGenerator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        AggregationDistinctProjection aggregationDistinctProjection = mock(AggregationDistinctProjection.class);
        String testAlias = "AVG_DERIVED_COUNT_0";
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue(testAlias)));
        when(aggregationDistinctProjection.getStartIndex()).thenReturn(0);
        when(aggregationDistinctProjection.getStopIndex()).thenReturn(2);
        String testDistinctInnerExpression = "TEST_DISTINCT_INNER_EXPRESSION";
        when(aggregationDistinctProjection.getDistinctInnerExpression()).thenReturn(testDistinctInnerExpression);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singletonList(aggregationDistinctProjection));
        AggregationDistinctTokenGenerator aggregationDistinctTokenGenerator = new AggregationDistinctTokenGenerator();
        List<SQLToken> generateSQLTokensResult = new ArrayList<>(aggregationDistinctTokenGenerator.generateSQLTokens(selectStatementContext));
        assertThat(generateSQLTokensResult.get(0).toString(), is(testDistinctInnerExpression + " AS " + testAlias));
        when(aggregationDistinctProjection.getAlias()).thenReturn(Optional.of(new IdentifierValue("TEST_ERROR_ALIAS")));
        generateSQLTokensResult = new ArrayList<>(aggregationDistinctTokenGenerator.generateSQLTokens(selectStatementContext));
        assertThat(generateSQLTokensResult.get(0).toString(), is(testDistinctInnerExpression));
    }
}
