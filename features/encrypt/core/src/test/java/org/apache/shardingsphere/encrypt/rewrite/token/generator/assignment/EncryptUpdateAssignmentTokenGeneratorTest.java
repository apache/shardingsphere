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
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptUpdateAssignmentTokenGeneratorTest {
    
    private EncryptUpdateAssignmentTokenGenerator tokenGenerator;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpdateStatementContext updateStatementContext;
    
    @BeforeEach
    void setup() {
        tokenGenerator = new EncryptUpdateAssignmentTokenGenerator(mockEncryptRule());
        when(updateStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue()).thenReturn("table");
        ColumnAssignmentSegment assignmentSegment = mock(ColumnAssignmentSegment.class);
        when(updateStatementContext.getSqlStatement().getSetAssignment().getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.findEncryptTable("table")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    @Test
    void assertIsGenerateSQLTokenUpdateSQLSuccess() {
        assertTrue(tokenGenerator.isGenerateSQLToken(updateStatementContext));
    }
}
