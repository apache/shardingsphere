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
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(InsertStatementHandler.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptAssignmentTokenGeneratorTest {
    
    private final EncryptAssignmentTokenGenerator tokenGenerator = new EncryptAssignmentTokenGenerator();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpdateStatementContext updateStatement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InsertStatementContext insertStatement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AssignmentSegment assignmentSegment;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SetAssignmentSegment setAssignmentSegment;
    
    @BeforeEach
    void setup() {
        tokenGenerator.setEncryptRule(mockEncryptRule());
        when(updateStatement.getAllTables().iterator().next().getTableName().getIdentifier().getValue()).thenReturn("table");
        when(updateStatement.getSqlStatement().getSetAssignment().getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
        when(assignmentSegment.getColumns().get(0).getIdentifier().getValue()).thenReturn("columns");
        when(insertStatement.getAllTables().iterator().next().getTableName().getIdentifier().getValue()).thenReturn("table");
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.isEncryptColumn("columns")).thenReturn(true);
        when(encryptTable.getEncryptColumn("columns")).thenReturn(mock(EncryptColumn.class, RETURNS_DEEP_STUBS));
        when(result.getEncryptTable("table")).thenReturn(encryptTable);
        return result;
    }
    
    @Test
    void assertIsGenerateSQLTokenUpdateSQLSuccess() {
        assertTrue(tokenGenerator.isGenerateSQLToken(updateStatement));
    }
    
    @Test
    void assertIsGenerateSQLTokenUpdateSQLFail() {
        when(InsertStatementHandler.getSetAssignmentSegment(any())).thenReturn(Optional.of(setAssignmentSegment));
        assertTrue(tokenGenerator.isGenerateSQLToken(insertStatement));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateParameterMarkerExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(mock(ParameterMarkerExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(updateStatement).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateLiteralExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(updateStatement).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateEmpty() {
        when(assignmentSegment.getValue()).thenReturn(null);
        assertTrue(tokenGenerator.generateSQLTokens(updateStatement).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokenWithInsertLiteralExpressionSegment() {
        when(InsertStatementHandler.getSetAssignmentSegment(any())).thenReturn(Optional.of(setAssignmentSegment));
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(insertStatement).size(), is(1));
    }
}
