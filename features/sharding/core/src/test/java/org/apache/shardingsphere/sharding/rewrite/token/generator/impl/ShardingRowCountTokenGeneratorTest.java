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

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.RowCountToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRowCountTokenGeneratorTest {
    
    private final ShardingRowCountTokenGenerator generator = new ShardingRowCountTokenGenerator();
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutRowCountSegment() {
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotNumberLiteralPaginationValueSegment() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getPaginationContext().getRowCountSegment()).thenReturn(Optional.of(mock(ParameterMarkerPaginationValueSegment.class)));
        assertFalse(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithNumberLiteralPaginationValueSegment() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getPaginationContext().getRowCountSegment()).thenReturn(Optional.of(mock(NumberLiteralPaginationValueSegment.class)));
        assertTrue(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        RowCountToken actual = generator.generateSQLToken(mockSelectStatementContext());
        assertThat(actual.toString(), is(String.valueOf(20L)));
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        NumberLiteralLimitValueSegment offsetSegment = new NumberLiteralLimitValueSegment(0, 0, 12L);
        NumberLiteralLimitValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(0, 0, 8L);
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getPaginationContext()).thenReturn(new PaginationContext(offsetSegment, rowCountSegment, null));
        when(result.isSameGroupByAndOrderByItems()).thenReturn(true);
        return result;
    }
}
