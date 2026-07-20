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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptAssignmentTokenGeneratorTest {
    
    private static MockedConstruction<DatabaseTypeRegistry> registryConstruction;
    
    private EncryptAssignmentTokenGenerator tokenGenerator;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TablesContext tablesContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ColumnAssignmentSegment assignmentSegment;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SetAssignmentSegment setAssignmentSegment;
    
    @BeforeAll
    static void beforeAll() {
        registryConstruction = mockConstruction(DatabaseTypeRegistry.class, (mock, mockContext) -> {
            DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
            when(dialectDatabaseMetaData.getQuoteCharacter()).thenReturn(QuoteCharacter.NONE);
            when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
        });
    }
    
    @AfterAll
    static void afterAll() {
        registryConstruction.close();
    }
    
    @BeforeEach
    void setup() {
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("columns"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("table"),
                new IdentifierValue("columns"), TableSourceType.PHYSICAL_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.isEncryptColumn("columns")).thenReturn(true);
        when(encryptTable.getEncryptColumn("columns")).thenReturn(mock(EncryptColumn.class, RETURNS_DEEP_STUBS));
        when(result.findEncryptTable("table")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateParameterMarkerExpressionSegment() {
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(mock(ParameterMarkerExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateLiteralExpressionSegment() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateEmpty() {
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(null);
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokenWithInsertLiteralExpressionSegment() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithOpenQueryLiteralExpressionSegment() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockOpenQueryEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ColumnSegment columnSegment = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(new LiteralExpressionSegment(124, 144, "Sales and Marketing"));
        when(assignmentSegment.getStopIndex()).thenReturn(144);
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("dbo"));
        Collection<SQLToken> actual = tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment, createOpenQueryTableSegment());
        Iterator<SQLToken> iterator = actual.iterator();
        assertThat(actual.size(), is(2));
        assertThat(iterator.next().toString(), is("group_name_cipher = 'encryptValue'"));
        assertThat(iterator.next().toString(), is("'SELECT group_name_cipher FROM dbo.Department WHERE DepartmentID = 4'"));
    }
    
    @Test
    void assertGenerateSQLTokenWithOpenQueryMultipleAssignments() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockOpenQueryMultiColumnEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        FunctionTableSegment openQueryTable = createMultiColumnOpenQueryTableSegment();
        SetAssignmentSegment multiAssignment = createMultiColumnSetAssignment();
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("dbo"));
        Collection<SQLToken> actual = tokenGenerator.generateSQLTokens(tablesContext, multiAssignment, openQueryTable);
        assertThat(actual.size(), is(3));
        Iterator<SQLToken> iterator = actual.iterator();
        assertThat(iterator.next().toString(), is("group_name_cipher = 'groupEncryptValue'"));
        assertThat(iterator.next().toString(), is("dept_code_cipher = 'deptEncryptValue'"));
        assertThat(iterator.next().toString(), is("'SELECT group_name_cipher, dept_code_cipher FROM dbo.Department WHERE DepartmentID = 4'"));
    }
    
    @Test
    void assertGenerateSQLTokenWithOpenQueryPreservesWhereClauseColumnRef() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockOpenQueryEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ColumnSegment columnSegment = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(new LiteralExpressionSegment(124, 144, "Sales and Marketing"));
        when(assignmentSegment.getStopIndex()).thenReturn(144);
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("dbo"));
        Collection<SQLToken> actual = tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment, createOpenQueryTableSegmentWithColumnInWhere());
        assertThat(actual.size(), is(2));
        Iterator<SQLToken> iterator = actual.iterator();
        iterator.next();
        assertThat(iterator.next().toString(), is("'SELECT group_name_cipher FROM dbo.Department WHERE GroupName IS NOT NULL'"));
    }
    
    @Test
    void assertGenerateSQLTokenWithOpenQueryDerivedColumns() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockOpenQueryDerivedColumnsEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        FunctionTableSegment openQueryTable = createDerivedColumnsOpenQueryTableSegment();
        SetAssignmentSegment multiAssignment = createDerivedColumnsSetAssignment();
        when(tablesContext.getSchemaName()).thenReturn(Optional.of("dbo"));
        Collection<SQLToken> actual = tokenGenerator.generateSQLTokens(tablesContext, multiAssignment, openQueryTable);
        assertThat(actual.size(), is(3));
        Iterator<SQLToken> iterator = actual.iterator();
        assertThat(iterator.next().toString(), is("group_name_cipher = 'groupEncryptValue'"));
        assertThat(iterator.next().toString(), is("remark_cipher = 'remarkCipherValue', assisted_query_remark = 'assistedValue', like_query_remark = 'likeValue'"));
        assertThat(iterator.next().toString(), is("'SELECT group_name_cipher, remark_cipher, assisted_query_remark, like_query_remark FROM dbo.Department WHERE DepartmentID = 4'"));
    }
    
    private EncryptRule mockOpenQueryEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("GroupName")).thenReturn(true);
        when(encryptTable.getTable()).thenReturn("Department");
        when(encryptTable.getEncryptColumn("GroupName")).thenReturn(encryptColumn);
        when(encryptColumn.getName()).thenReturn("GroupName");
        when(encryptColumn.getCipher().getName()).thenReturn("group_name_cipher");
        when(encryptColumn.getCipher().encrypt("foo_db", "dbo", "Department", "GroupName", Collections.singletonList("Sales and Marketing")))
                .thenReturn(Collections.singletonList("encryptValue"));
        return result;
    }
    
    private FunctionTableSegment createOpenQueryTableSegment() {
        FunctionSegment functionSegment = new FunctionSegment(7, 106, "OPENQUERY", "OPENQUERY (MyLinkedServer, 'SELECT GroupName FROM dbo.Department WHERE DepartmentID = 4')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 95, "SELECT GroupName FROM dbo.Department WHERE DepartmentID = 4"));
        return new FunctionTableSegment(7, 106, functionSegment);
    }
    
    private FunctionTableSegment createOpenQueryTableSegmentWithColumnInWhere() {
        FunctionSegment functionSegment = new FunctionSegment(7, 115, "OPENQUERY",
                "OPENQUERY (MyLinkedServer, 'SELECT GroupName FROM dbo.Department WHERE GroupName IS NOT NULL')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 104, "SELECT GroupName FROM dbo.Department WHERE GroupName IS NOT NULL"));
        return new FunctionTableSegment(7, 115, functionSegment);
    }
    
    private EncryptRule mockOpenQueryMultiColumnEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn groupNameColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        EncryptColumn deptCodeColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("GroupName")).thenReturn(true);
        when(encryptTable.isEncryptColumn("DeptCode")).thenReturn(true);
        when(encryptTable.getTable()).thenReturn("Department");
        when(encryptTable.getEncryptColumn("GroupName")).thenReturn(groupNameColumn);
        when(encryptTable.getEncryptColumn("DeptCode")).thenReturn(deptCodeColumn);
        when(groupNameColumn.getName()).thenReturn("GroupName");
        when(groupNameColumn.getCipher().getName()).thenReturn("group_name_cipher");
        when(groupNameColumn.getCipher().encrypt("foo_db", "dbo", "Department", "GroupName", Collections.singletonList("Sales")))
                .thenReturn(Collections.singletonList("groupEncryptValue"));
        when(deptCodeColumn.getName()).thenReturn("DeptCode");
        when(deptCodeColumn.getCipher().getName()).thenReturn("dept_code_cipher");
        when(deptCodeColumn.getCipher().encrypt("foo_db", "dbo", "Department", "DeptCode", Collections.singletonList("D001")))
                .thenReturn(Collections.singletonList("deptEncryptValue"));
        return result;
    }
    
    private SetAssignmentSegment createMultiColumnSetAssignment() {
        ColumnSegment groupNameCol = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        groupNameCol.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        ColumnAssignmentSegment groupNameAssignment = mock(ColumnAssignmentSegment.class);
        when(groupNameAssignment.getColumns()).thenReturn(Collections.singletonList(groupNameCol));
        when(groupNameAssignment.getValue()).thenReturn(new LiteralExpressionSegment(124, 128, "Sales"));
        when(groupNameAssignment.getStopIndex()).thenReturn(128);
        ColumnSegment deptCodeCol = new ColumnSegment(132, 139, new IdentifierValue("DeptCode"));
        deptCodeCol.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("DeptCode"), TableSourceType.TEMPORARY_TABLE));
        ColumnAssignmentSegment deptCodeAssignment = mock(ColumnAssignmentSegment.class);
        when(deptCodeAssignment.getColumns()).thenReturn(Collections.singletonList(deptCodeCol));
        when(deptCodeAssignment.getValue()).thenReturn(new LiteralExpressionSegment(143, 146, "D001"));
        when(deptCodeAssignment.getStopIndex()).thenReturn(146);
        SetAssignmentSegment result = mock(SetAssignmentSegment.class);
        when(result.getAssignments()).thenReturn(Arrays.asList(groupNameAssignment, deptCodeAssignment));
        return result;
    }
    
    private FunctionTableSegment createMultiColumnOpenQueryTableSegment() {
        FunctionSegment functionSegment = new FunctionSegment(7, 110, "OPENQUERY",
                "OPENQUERY (MyLinkedServer, 'SELECT GroupName, DeptCode FROM dbo.Department WHERE DepartmentID = 4')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 99, "SELECT GroupName, DeptCode FROM dbo.Department WHERE DepartmentID = 4"));
        return new FunctionTableSegment(7, 110, functionSegment);
    }
    
    private EncryptRule mockOpenQueryDerivedColumnsEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn groupNameColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        EncryptColumn remarkColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        EncryptAlgorithm assistedEncryptor = mock(EncryptAlgorithm.class);
        EncryptAlgorithm likeEncryptor = mock(EncryptAlgorithm.class);
        when(assistedEncryptor.encrypt(eq("note"), any())).thenReturn("assistedValue");
        when(likeEncryptor.encrypt(eq("note"), any())).thenReturn("likeValue");
        when(result.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("GroupName")).thenReturn(true);
        when(encryptTable.isEncryptColumn("Remark")).thenReturn(true);
        when(encryptTable.getTable()).thenReturn("Department");
        when(encryptTable.getEncryptColumn("GroupName")).thenReturn(groupNameColumn);
        when(encryptTable.getEncryptColumn("Remark")).thenReturn(remarkColumn);
        when(groupNameColumn.getName()).thenReturn("GroupName");
        when(groupNameColumn.getCipher().getName()).thenReturn("group_name_cipher");
        when(groupNameColumn.getCipher().encrypt("foo_db", "dbo", "Department", "GroupName", Collections.singletonList("Sales")))
                .thenReturn(Collections.singletonList("groupEncryptValue"));
        when(groupNameColumn.getAssistedQuery()).thenReturn(Optional.empty());
        when(groupNameColumn.getLikeQuery()).thenReturn(Optional.empty());
        when(remarkColumn.getName()).thenReturn("Remark");
        when(remarkColumn.getCipher().getName()).thenReturn("remark_cipher");
        when(remarkColumn.getCipher().encrypt("foo_db", "dbo", "Department", "Remark", Collections.singletonList("note")))
                .thenReturn(Collections.singletonList("remarkCipherValue"));
        when(remarkColumn.getAssistedQuery()).thenReturn(Optional.of(new AssistedQueryColumnItem("assisted_query_remark", assistedEncryptor)));
        when(remarkColumn.getLikeQuery()).thenReturn(Optional.of(new LikeQueryColumnItem("like_query_remark", likeEncryptor)));
        return result;
    }
    
    private SetAssignmentSegment createDerivedColumnsSetAssignment() {
        ColumnSegment groupNameCol = new ColumnSegment(112, 120, new IdentifierValue("GroupName"));
        groupNameCol.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("GroupName"), TableSourceType.TEMPORARY_TABLE));
        ColumnAssignmentSegment groupNameAssignment = mock(ColumnAssignmentSegment.class);
        when(groupNameAssignment.getColumns()).thenReturn(Collections.singletonList(groupNameCol));
        when(groupNameAssignment.getValue()).thenReturn(new LiteralExpressionSegment(124, 128, "Sales"));
        when(groupNameAssignment.getStopIndex()).thenReturn(128);
        ColumnSegment remarkCol = new ColumnSegment(132, 137, new IdentifierValue("Remark"));
        remarkCol.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, new IdentifierValue("Remark"), TableSourceType.TEMPORARY_TABLE));
        ColumnAssignmentSegment remarkAssignment = mock(ColumnAssignmentSegment.class);
        when(remarkAssignment.getColumns()).thenReturn(Collections.singletonList(remarkCol));
        when(remarkAssignment.getValue()).thenReturn(new LiteralExpressionSegment(141, 144, "note"));
        when(remarkAssignment.getStopIndex()).thenReturn(144);
        SetAssignmentSegment result = mock(SetAssignmentSegment.class);
        when(result.getAssignments()).thenReturn(Arrays.asList(groupNameAssignment, remarkAssignment));
        return result;
    }
    
    private FunctionTableSegment createDerivedColumnsOpenQueryTableSegment() {
        FunctionSegment functionSegment = new FunctionSegment(7, 108, "OPENQUERY",
                "OPENQUERY (MyLinkedServer, 'SELECT GroupName, Remark FROM dbo.Department WHERE DepartmentID = 4')");
        functionSegment.getParameters().add(new ColumnSegment(18, 31, new IdentifierValue("MyLinkedServer")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(34, 97, "SELECT GroupName, Remark FROM dbo.Department WHERE DepartmentID = 4"));
        return new FunctionTableSegment(7, 108, functionSegment);
    }
}
