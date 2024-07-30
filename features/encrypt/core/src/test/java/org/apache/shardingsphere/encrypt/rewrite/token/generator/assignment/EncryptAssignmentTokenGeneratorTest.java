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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptAssignmentTokenGeneratorTest {
    
    private EncryptAssignmentTokenGenerator tokenGenerator;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TablesContext tablesContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ColumnAssignmentSegment assignmentSegment;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SetAssignmentSegment setAssignmentSegment;
    
    @BeforeEach
    void setup() {
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), null, null);
        when(tablesContext.getSimpleTables().iterator().next().getTableName().getIdentifier().getValue()).thenReturn("table");
        when(assignmentSegment.getColumns().get(0).getIdentifier().getValue()).thenReturn("columns");
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
    void assertGenerateSQLTokenWithUpdateParameterMarkerExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(mock(ParameterMarkerExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateLiteralExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateEmpty() {
        when(assignmentSegment.getValue()).thenReturn(null);
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokenWithInsertLiteralExpressionSegment() {
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
}
