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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptAssignmentTokenGeneratorOpenQueryUnsupportedShapeTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TablesContext tablesContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ColumnAssignmentSegment assignmentSegment;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SetAssignmentSegment setAssignmentSegment;
    
    @Test
    void assertGenerateSQLTokenWithCommaTableSourcesExpectsException() {
        ColumnSegment columnSegment = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
        EncryptAssignmentTokenGenerator tokenGenerator = new EncryptAssignmentTokenGenerator(mockOpenQueryEncryptRule(), mock(ShardingSphereDatabase.class),
                TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertThrows(UnsupportedEncryptSQLException.class,
                () -> tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment, createOpenQueryTableSegmentWithCommaTableSources()));
    }
    
    @Test
    void assertGenerateSQLTokenWithOpenQueryUnsetEncryptColumnInWhereExpectsException() {
        ColumnSegment columnSegment = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(mock(ParameterMarkerExpressionSegment.class));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("dbo"));
        EncryptAssignmentTokenGenerator tokenGenerator = new EncryptAssignmentTokenGenerator(mockOpenQueryEncryptRuleWithUnsetColumn(), mock(ShardingSphereDatabase.class),
                TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertThrows(UnsupportedEncryptSQLException.class,
                () -> tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment, createOpenQueryTableSegmentWithExtraColumnInWhere()));
    }
    
    @Test
    void assertGenerateSQLTokenWithOpenQueryUnsupportedAssignmentExpressionExpectsException() {
        ColumnSegment columnSegment = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(new FunctionSegment(124, 134, "UPPER", "UPPER('x')"));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
        EncryptAssignmentTokenGenerator tokenGenerator = new EncryptAssignmentTokenGenerator(
                mockOpenQueryEncryptRuleWithTable(), mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertThrows(UnsupportedEncryptSQLException.class,
                () -> tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment, createOpenQueryTableSegment()));
    }
    
    private EncryptRule mockOpenQueryEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("GroupName")).thenReturn(true);
        when(encryptTable.getEncryptColumn("GroupName")).thenReturn(encryptColumn);
        return result;
    }
    
    private EncryptRule mockOpenQueryEncryptRuleWithUnsetColumn() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn groupNameColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        EncryptColumn extraColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("GroupName")).thenReturn(true);
        when(encryptTable.getTable()).thenReturn("Department");
        when(encryptTable.getEncryptColumn("GroupName")).thenReturn(groupNameColumn);
        when(encryptTable.getEncryptColumns()).thenReturn(Arrays.asList(groupNameColumn, extraColumn));
        when(groupNameColumn.getName()).thenReturn("GroupName");
        when(groupNameColumn.getCipher().getName()).thenReturn("group_name_cipher");
        when(extraColumn.getName()).thenReturn("ExtraCol");
        return result;
    }
    
    private FunctionTableSegment createOpenQueryTableSegmentWithCommaTableSources() {
        FunctionSegment functionSegment = new FunctionSegment(7, 95, "OPENQUERY",
                "OPENQUERY (MyLinkedServer, 'SELECT GroupName FROM dbo.Department, dbo.Other')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 84, "SELECT GroupName FROM dbo.Department, dbo.Other"));
        return new FunctionTableSegment(7, 95, functionSegment);
    }
    
    private FunctionTableSegment createOpenQueryTableSegmentWithExtraColumnInWhere() {
        FunctionSegment functionSegment = new FunctionSegment(7, 112, "OPENQUERY",
                "OPENQUERY (MyLinkedServer, 'SELECT GroupName FROM dbo.Department WHERE ExtraCol IS NOT NULL')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 101, "SELECT GroupName FROM dbo.Department WHERE ExtraCol IS NOT NULL"));
        return new FunctionTableSegment(7, 112, functionSegment);
    }
    
    private EncryptRule mockOpenQueryEncryptRuleWithTable() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("GroupName")).thenReturn(true);
        when(encryptTable.getTable()).thenReturn("Department");
        when(encryptTable.getEncryptColumn("GroupName")).thenReturn(encryptColumn);
        return result;
    }
    
    private FunctionTableSegment createOpenQueryTableSegment() {
        FunctionSegment functionSegment = new FunctionSegment(7, 106, "OPENQUERY",
                "OPENQUERY (MyLinkedServer, 'SELECT GroupName FROM dbo.Department WHERE DepartmentID = 4')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 95, "SELECT GroupName FROM dbo.Department WHERE DepartmentID = 4"));
        return new FunctionTableSegment(7, 106, functionSegment);
    }
}
