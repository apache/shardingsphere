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

package org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator;

import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratedKeyInsertColumnTokenGeneratorTest {
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutInsertColumns() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getInsertColumns()).thenReturn(Optional.empty());
        assertFalse(new GeneratedKeyInsertColumnTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyInsertColumns() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(0, 0, Collections.emptyList())));
        assertFalse(new GeneratedKeyInsertColumnTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutGeneratedKeyContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(0, 0, Collections.singleton(mock(ColumnSegment.class)))));
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        assertFalse(new GeneratedKeyInsertColumnTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyGeneratedValues() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(0, 0, Collections.singleton(mock(ColumnSegment.class)))));
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.of(new GeneratedKeyContext("foo_col", false)));
        assertFalse(new GeneratedKeyInsertColumnTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(0, 0, Collections.singleton(mock(ColumnSegment.class)))));
        GeneratedKeyContext generatedKeyContext = new GeneratedKeyContext("foo_col", false);
        generatedKeyContext.getGeneratedValues().add(1);
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        assertTrue(new GeneratedKeyInsertColumnTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.of(new GeneratedKeyContext("foo_col", false)));
        when(statementContext.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(0, 4, Collections.emptyList())));
        assertThat(new GeneratedKeyInsertColumnTokenGenerator().generateSQLToken(statementContext).toString(), is(", foo_col"));
    }
}
