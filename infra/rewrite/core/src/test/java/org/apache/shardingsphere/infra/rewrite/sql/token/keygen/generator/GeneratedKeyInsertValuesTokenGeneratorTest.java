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
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratedKeyInsertValuesTokenGeneratorTest {
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyInsertValues() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getValues()).thenReturn(Collections.emptyList());
        assertFalse(new GeneratedKeyInsertValuesTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutGeneratedKeyContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getValues()).thenReturn(Collections.singleton(mock(InsertValuesSegment.class)));
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        assertFalse(new GeneratedKeyInsertValuesTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyGeneratedValues() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getValues()).thenReturn(Collections.singleton(mock(InsertValuesSegment.class)));
        GeneratedKeyContext generatedKeyContext = new GeneratedKeyContext("foo_col", false);
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        assertFalse(new GeneratedKeyInsertValuesTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement().getValues()).thenReturn(Collections.singleton(mock(InsertValuesSegment.class)));
        GeneratedKeyContext generatedKeyContext = new GeneratedKeyContext("foo_col", false);
        generatedKeyContext.getGeneratedValues().add(1);
        when(statementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        assertTrue(new GeneratedKeyInsertValuesTokenGenerator().isGenerateSQLToken(statementContext));
    }
    
    @Test
    void assertGenerateSQLTokenWithoutPreviousInsertValuesTokens() {
        GeneratedKeyInsertValuesTokenGenerator generator = new GeneratedKeyInsertValuesTokenGenerator();
        generator.setPreviousSQLTokens(Collections.singletonList(mock(SQLToken.class)));
        assertThrows(IllegalStateException.class, () -> generator.generateSQLToken(mock(InsertStatementContext.class)));
    }
    
    @Test
    void assertGenerateSQLTokenWithPreviousSQLTokens() {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(mock(InsertValueContext.class)));
        when(sqlStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext()));
        List<List<Object>> parameterGroups = Collections.singletonList(new ArrayList<>(Collections.singleton(new Object())));
        when(sqlStatementContext.getGroupedParameters()).thenReturn(parameterGroups);
        GeneratedKeyInsertValuesTokenGenerator generator = new GeneratedKeyInsertValuesTokenGenerator();
        generator.setPreviousSQLTokens(getPreviousSQLTokens());
        SQLToken sqlToken = generator.generateSQLToken(sqlStatementContext);
        assertThat(sqlToken, isA(InsertValuesToken.class));
        assertThat(((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().get(0), isA(DerivedParameterMarkerExpressionSegment.class));
        parameterGroups.get(0).clear();
        ((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().clear();
        sqlToken = generator.generateSQLToken(sqlStatementContext);
        assertThat(((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().get(0), isA(DerivedLiteralExpressionSegment.class));
    }
    
    private GeneratedKeyContext createGeneratedKeyContext() {
        GeneratedKeyContext result = new GeneratedKeyContext("foo_col", false);
        result.getGeneratedValues().add("TEST_GENERATED_VALUE");
        return result;
    }
    
    private List<SQLToken> getPreviousSQLTokens() {
        InsertValue insertValue = mock(InsertValue.class);
        when(insertValue.getValues()).thenReturn(new LinkedList<>());
        InsertValuesToken sqlToken = mock(InsertValuesToken.class);
        when(sqlToken.getInsertValues()).thenReturn(Collections.singletonList(insertValue));
        return Collections.singletonList(sqlToken);
    }
}
