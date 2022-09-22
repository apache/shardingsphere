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

import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.RowCountTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.RowCountToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RowCountTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        RowCountTokenGenerator rowCountTokenGenerator = new RowCountTokenGenerator();
        assertFalse(rowCountTokenGenerator.isGenerateSQLToken(insertStatementContext));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getPaginationContext().getRowCountSegment().isPresent()).thenReturn(Boolean.FALSE);
        NumberLiteralPaginationValueSegment numberLiteralPaginationValueSegment = mock(NumberLiteralPaginationValueSegment.class);
        assertFalse(rowCountTokenGenerator.isGenerateSQLToken(selectStatementContext));
        when(selectStatementContext.getPaginationContext().getRowCountSegment()).thenReturn(Optional.of(numberLiteralPaginationValueSegment));
        assertTrue(rowCountTokenGenerator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    public void assertGenerateSQLToken() {
        final int testOffsetSegmentValue = 12;
        NumberLiteralLimitValueSegment offsetSegment = new NumberLiteralLimitValueSegment(1, 2, testOffsetSegmentValue);
        final int testRowCountSegmentValue = 8;
        NumberLiteralLimitValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(4, 5, testRowCountSegmentValue);
        PaginationContext paginationContext = new PaginationContext(offsetSegment, rowCountSegment, null);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getPaginationContext()).thenReturn(paginationContext);
        when(selectStatementContext.getGroupByContext().getItems().isEmpty()).thenReturn(Boolean.FALSE);
        when(selectStatementContext.isSameGroupByAndOrderByItems()).thenReturn(Boolean.FALSE);
        RowCountTokenGenerator rowCountTokenGenerator = new RowCountTokenGenerator();
        RowCountToken rowCountToken = rowCountTokenGenerator.generateSQLToken(selectStatementContext);
        assertThat(rowCountToken.toString(), is(String.valueOf(Integer.MAX_VALUE)));
        when(selectStatementContext.isSameGroupByAndOrderByItems()).thenReturn(Boolean.TRUE);
        rowCountToken = rowCountTokenGenerator.generateSQLToken(selectStatementContext);
        assertThat(rowCountToken.toString(), is(String.valueOf(testOffsetSegmentValue + testRowCountSegmentValue)));
    }
}
