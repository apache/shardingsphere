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

package org.apache.shardingsphere.encrypt.rewrite.token;

import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptInsertOnUpdateTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptInsertOnUpdateTokenGeneratorTest {
    
    private EncryptInsertOnUpdateTokenGenerator generator;
    
    @Before
    public void setup() {
        generator = new EncryptInsertOnUpdateTokenGenerator();
        generator.setEncryptRule(mockEncryptRule());
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        when(result.getCipherColumn("t_user", "mobile")).thenReturn("cipher_mobile");
        when(result.findEncryptor("t_user", "mobile")).thenReturn(Optional.of(mock(EncryptAlgorithm.class)));
        when(result.findEncryptor("t_user", "cipher_mobile")).thenReturn(Optional.of(mock(EncryptAlgorithm.class)));
        when(result.getEncryptValues(null, "db_test", "t_user", "mobile", Collections.singletonList(0))).thenReturn(Collections.singletonList("encryptValue"));
        return result;
    }
    
    @Test
    public void assertIsNotGenerateSQLTokenWithNotInsertStatement() {
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class)));
    }
    
    @Test
    public void assertIsNotGenerateSQLTokenWithoutOnDuplicateKeyColumns() {
        assertFalse(generator.isGenerateSQLToken(mock(InsertStatementContext.class)));
    }
    
    @Test
    public void assertIsGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getSqlStatement()).thenReturn(mock(MySQLInsertStatement.class));
        when(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(
                insertStatementContext.getSqlStatement())).thenReturn(Optional.of(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList())));
        assertTrue(generator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    public void assertGenerateSQLTokens() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.of("db_test"));
        MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class, RETURNS_DEEP_STUBS);
        when(insertStatement.getTable().getTableName().getIdentifier().getValue()).thenReturn("t_user");
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);
        when(onDuplicateKeyColumnsSegment.getColumns()).thenReturn(buildAssignmentSegment());
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatementContext.getSqlStatement())).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        Iterator<EncryptAssignmentToken> actual = generator.generateSQLTokens(insertStatementContext).iterator();
        assertEncryptAssignmentToken(actual.next(), "cipher_mobile = ?");
        assertEncryptAssignmentToken(actual.next(), "cipher_mobile = VALUES(cipher_mobile)");
        assertEncryptAssignmentToken(actual.next(), "cipher_mobile = 'encryptValue'");
        assertFalse(actual.hasNext());
    }
    
    private Collection<AssignmentSegment> buildAssignmentSegment() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        List<ColumnSegment> columnSegments = Collections.singletonList(columnSegment);
        AssignmentSegment assignmentSegment1 = new ColumnAssignmentSegment(0, 0, columnSegments, new ParameterMarkerExpressionSegment(0, 0, 0));
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "VALUES", "VALUES (0)");
        functionSegment.getParameters().add(columnSegment);
        AssignmentSegment assignmentSegment2 = new ColumnAssignmentSegment(0, 0, columnSegments, functionSegment);
        AssignmentSegment assignmentSegment3 = new ColumnAssignmentSegment(0, 0, columnSegments, new LiteralExpressionSegment(0, 0, 0));
        return Arrays.asList(assignmentSegment1, assignmentSegment2, assignmentSegment3);
    }
    
    private void assertEncryptAssignmentToken(final EncryptAssignmentToken actual, final String expectedValue) {
        assertThat(actual.toString(), is(expectedValue));
        assertThat(actual.getStartIndex(), is(0));
        assertThat(actual.getStopIndex(), is(0));
    }
}
