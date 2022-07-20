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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class EncryptAssignmentTokenGeneratorTest {
    
    private final EncryptAssignmentTokenGenerator tokenGenerator = new EncryptAssignmentTokenGenerator();
    
    private UpdateStatementContext updateStatement;
    
    private InsertStatementContext insertStatement;
    
    private AssignmentSegment assignmentSegment;
    
    private SetAssignmentSegment setAssignmentSegment;
    
    private LiteralExpressionSegment literalExpression;
    
    private ParameterMarkerExpressionSegment parameterMarkerExpression;
    
    @Before
    public void setup() {
        updateStatement = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        insertStatement = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        assignmentSegment = mock(AssignmentSegment.class, RETURNS_DEEP_STUBS);
        setAssignmentSegment = mock(SetAssignmentSegment.class, RETURNS_DEEP_STUBS);
        literalExpression = mock(LiteralExpressionSegment.class, RETURNS_DEEP_STUBS);
        parameterMarkerExpression = mock(ParameterMarkerExpressionSegment.class, RETURNS_DEEP_STUBS);
        EncryptRule encryptRule = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        tokenGenerator.setEncryptRule(encryptRule);
        when(updateStatement.getAllTables().iterator().next().getTableName().getIdentifier().getValue()).thenReturn("table");
        when(updateStatement.getSqlStatement().getSetAssignment().getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns().get(0).getIdentifier().getValue()).thenReturn("columns");
        when(encryptRule.findEncryptor(eq("table"), eq("columns"))).thenReturn(Optional.of(mock(EncryptAlgorithm.class)));
        when(insertStatement.getAllTables().iterator().next().getTableName().getIdentifier().getValue()).thenReturn("table");
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
    }
    
    @Test
    public void assertIsGenerateSQLTokenUpdateSQLSuccess() {
        assertTrue(tokenGenerator.isGenerateSQLToken(updateStatement));
    }
    
    @Test
    public void assertIsGenerateSQLTokenUpdateSQLFail() {
        try (MockedStatic<InsertStatementHandler> insertStatementHandlerMockedStatic = mockStatic(InsertStatementHandler.class)) {
            insertStatementHandlerMockedStatic.when(() -> InsertStatementHandler.getSetAssignmentSegment(any())).thenReturn(Optional.of(setAssignmentSegment));
            assertTrue(tokenGenerator.isGenerateSQLToken(insertStatement));
        }
    }
    
    @Test
    public void assertGenerateSQLTokenWithUpdateParameterMarkerExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(parameterMarkerExpression);
        assertThat(tokenGenerator.generateSQLTokens(updateStatement).size(), is(1));
    }
    
    @Test
    public void assertGenerateSQLTokenWithUpdateLiteralExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(literalExpression);
        assertThat(tokenGenerator.generateSQLTokens(updateStatement).size(), is(1));
    }
    
    @Test
    public void assertGenerateSQLTokenWithUpdateEmpty() {
        when(assignmentSegment.getValue()).thenReturn(null);
        assertTrue(tokenGenerator.generateSQLTokens(updateStatement).isEmpty());
    }
    
    @Test
    public void assertGenerateSQLTokenWithInsertLiteralExpressionSegment() {
        try (MockedStatic<InsertStatementHandler> insertStatementHandlerMockedStatic = mockStatic(InsertStatementHandler.class)) {
            insertStatementHandlerMockedStatic.when(() -> InsertStatementHandler.getSetAssignmentSegment(any())).thenReturn(Optional.of(setAssignmentSegment));
            when(assignmentSegment.getValue()).thenReturn(literalExpression);
            assertThat(tokenGenerator.generateSQLTokens(insertStatement).size(), is(1));
        }
    }
}
