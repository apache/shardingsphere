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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.insert;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class EncryptInsertOnUpdateTokenGeneratorTest {
    
    private static MockedConstruction<DatabaseTypeRegistry> registryConstruction;
    
    private EncryptInsertOnUpdateTokenGenerator generator;
    
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
        generator = new EncryptInsertOnUpdateTokenGenerator(mockEncryptRule(), mock(ShardingSphereDatabase.class));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptColumn encryptColumn = mockEncryptColumn();
        EncryptTable encryptTable = mockEncryptTable();
        when(encryptTable.getEncryptColumn("mobile")).thenReturn(encryptColumn);
        when(result.getEncryptTable("t_user")).thenReturn(encryptTable);
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        EncryptColumn result = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.getCipher().getName()).thenReturn("cipher_mobile");
        when(result.getCipher().encrypt(null, "db_test", "t_user", "mobile", Collections.singletonList(0))).thenReturn(Collections.singletonList("encryptValue"));
        return result;
    }
    
    private static EncryptTable mockEncryptTable() {
        EncryptTable result = mock(EncryptTable.class, RETURNS_DEEP_STUBS);
        when(result.getTable()).thenReturn("t_user");
        when(result.isEncryptColumn("mobile")).thenReturn(true);
        when(result.getEncryptColumn("mobile").getCipher().getName()).thenReturn("cipher_mobile");
        return result;
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotInsertStatement() {
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutOnDuplicateKeyColumns() {
        assertFalse(generator.isGenerateSQLToken(mock(InsertStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList())));
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        assertTrue(generator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokens() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.of("db_test"));
        InsertStatement insertStatement = mock(InsertStatement.class, RETURNS_DEEP_STUBS);
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))));
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);
        when(onDuplicateKeyColumnsSegment.getColumns()).thenReturn(buildAssignmentSegment());
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        Iterator<SQLToken> actual = generator.generateSQLTokens(insertStatementContext).iterator();
        assertEncryptAssignmentToken((EncryptAssignmentToken) actual.next(), "cipher_mobile = ?");
        assertEncryptAssignmentToken((EncryptAssignmentToken) actual.next(), "cipher_mobile = VALUES(cipher_mobile)");
        assertEncryptAssignmentToken((EncryptAssignmentToken) actual.next(), "cipher_mobile = 'encryptValue'");
        assertFalse(actual.hasNext());
    }
    
    private Collection<ColumnAssignmentSegment> buildAssignmentSegment() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        List<ColumnSegment> columnSegments = Collections.singletonList(columnSegment);
        ColumnAssignmentSegment assignmentSegment1 = new ColumnAssignmentSegment(0, 0, columnSegments, new ParameterMarkerExpressionSegment(0, 0, 0));
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "VALUES", "VALUES (0)");
        functionSegment.getParameters().add(columnSegment);
        ColumnAssignmentSegment assignmentSegment2 = new ColumnAssignmentSegment(0, 0, columnSegments, functionSegment);
        ColumnAssignmentSegment assignmentSegment3 = new ColumnAssignmentSegment(0, 0, columnSegments, new LiteralExpressionSegment(0, 0, 0));
        return Arrays.asList(assignmentSegment1, assignmentSegment2, assignmentSegment3);
    }
    
    private void assertEncryptAssignmentToken(final EncryptAssignmentToken actual, final String expectedValue) {
        assertThat(actual.toString(), is(expectedValue));
        assertThat(actual.getStartIndex(), is(0));
        assertThat(actual.getStopIndex(), is(0));
    }
}
