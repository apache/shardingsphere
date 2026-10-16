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
import org.apache.shardingsphere.infra.rewrite.sql.impl.DefaultSQLBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.RowCountToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRowCountTokenGeneratorTest {
    
    private final ShardingRowCountTokenGenerator generator = new ShardingRowCountTokenGenerator();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNotGenerateSQLTokenContexts")
    void assertIsNotGenerateSQLToken(final String name, final SQLStatementContext sqlStatementContext) {
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    private static Stream<Arguments> getNotGenerateSQLTokenContexts() {
        SelectStatementContext parameterMarkerContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(parameterMarkerContext.getPaginationContext().getRowCountSegment()).thenReturn(Optional.of(mock(ParameterMarkerPaginationValueSegment.class)));
        return Stream.of(
                Arguments.of("not select", mock(SQLStatementContext.class)),
                Arguments.of("without row count", mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)),
                Arguments.of("parameter marker row count", parameterMarkerContext));
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getImplicitFetchCases")
    void assertGenerateSQLTokenWithImplicitFetch(final String name, final String sql, final String expectedSQL) {
        int offsetIndex = sql.indexOf("12");
        int rowCountIndex = sql.toUpperCase(Locale.ROOT).indexOf("ROW", sql.toUpperCase(Locale.ROOT).indexOf("FETCH"));
        NumberLiteralLimitValueSegment offset = new NumberLiteralLimitValueSegment(offsetIndex, offsetIndex + 1, 12L);
        NumberLiteralLimitValueSegment rowCount = new NumberLiteralLimitValueSegment(rowCountIndex, rowCountIndex - 1, 1L);
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getPaginationContext()).thenReturn(new PaginationContext(offset, rowCount, null));
        when(statementContext.isSameGroupByAndOrderByItems()).thenReturn(true);
        RowCountToken actual = generator.generateSQLToken(statementContext);
        List<SQLToken> tokens = Arrays.asList(new ShardingOffsetTokenGenerator().generateSQLToken(statementContext), actual);
        assertThat(new DefaultSQLBuilder(sql, tokens).toSQL(), is(expectedSQL));
    }
    
    private static Stream<Arguments> getImplicitFetchCases() {
        return Stream.of(
                Arguments.of("FIRST after offset", "SELECT 1 OFFSET 12 ROWS FETCH FIRST ROW ONLY", "SELECT 1 OFFSET 0 ROWS FETCH FIRST 13 ROW ONLY"),
                Arguments.of("NEXT before offset", "SELECT 1 FETCH NEXT ROWS ONLY OFFSET 12 ROWS", "SELECT 1 FETCH NEXT 13 ROWS ONLY OFFSET 0 ROWS"),
                Arguments.of("lowercase and comment", "select 1 offset 12 rows fetch first /*gap*/ row only", "select 1 offset 0 rows fetch first /*gap*/ 13 row only"));
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
