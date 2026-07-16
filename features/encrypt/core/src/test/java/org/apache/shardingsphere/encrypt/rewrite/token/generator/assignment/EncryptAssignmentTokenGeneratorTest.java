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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
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
    
    private EncryptRule mockOpenQueryEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.getAllTableNames()).thenReturn(Collections.singleton("Department"));
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
}
