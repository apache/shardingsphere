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
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
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
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpdateStatementContext updateStatementContext;
    
    @Test
    void assertIsGenerateSQLTokenUpdateSQLSuccess() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.findEncryptTable("table")).thenReturn(Optional.of(mock(EncryptTable.class)));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("table"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")));
        when(updateStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(tableNameSegment)));
        when(updateStatementContext.getSqlStatement().getSetAssignment().getAssignments()).thenReturn(Collections.singleton(mock(ColumnAssignmentSegment.class)));
        EncryptUpdateAssignmentTokenGenerator tokenGenerator = new EncryptUpdateAssignmentTokenGenerator(encryptRule, mock(ShardingSphereDatabase.class));
        assertTrue(tokenGenerator.isGenerateSQLToken(updateStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithOpenQueryTarget() {
        EncryptUpdateAssignmentTokenGenerator tokenGenerator = new EncryptUpdateAssignmentTokenGenerator(mock(EncryptRule.class), mock(ShardingSphereDatabase.class));
        when(updateStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.emptyList());
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "OPENQUERY", "OPENQUERY (foo_server, 'SELECT foo_col FROM foo_schema.foo_tbl')");
        when(updateStatementContext.getSqlStatement().getTable()).thenReturn(new FunctionTableSegment(0, 0, functionSegment));
        assertTrue(tokenGenerator.isGenerateSQLToken(updateStatementContext));
    }
}
