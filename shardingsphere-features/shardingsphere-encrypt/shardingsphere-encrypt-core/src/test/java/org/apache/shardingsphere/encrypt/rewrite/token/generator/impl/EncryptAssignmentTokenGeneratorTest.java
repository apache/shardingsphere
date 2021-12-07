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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptAssignmentTokenGeneratorTest {

    @InjectMocks
    private EncryptAssignmentTokenGenerator tokenGenerator;

    @Test
    public void isGenerateSQLTokenForEncryptForUpdateTest() {

        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);

        assertTrue(tokenGenerator.isGenerateSQLTokenForEncrypt(updateStatementContext));
    }

    @Test
    public void isGenerateSQLTokenForEncryptForInsertTest() {

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));

        assertTrue(tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext));
    }

    @Test
    public void generateParameterSQLTokenTest() {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = mock(ParameterMarkerExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(assignmentSegment.getValue()).thenReturn(parameterMarkerExpressionSegment);

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertEquals(1, tokens.size());
    }

    @Test
    public void generateLiteralSQLTokenTest() {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final LiteralExpressionSegment literalExpressionSegment = mock(LiteralExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(assignmentSegment.getValue()).thenReturn(literalExpressionSegment);
        when(literalExpressionSegment.getLiterals()).thenReturn(new Object());
        when(encryptRule.getEncryptValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));
        when(encryptRule.getCipherColumn(anyString(), anyString())).thenReturn("col1");
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("col1"));
        when(encryptRule.getEncryptAssistedQueryValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertEquals(1, tokens.size());
    }

    @Test
    public void generateTokenForOtherSegementsTest() {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ExpressionSegment expressionSegment = mock(ExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(assignmentSegment.getValue()).thenReturn(expressionSegment);


        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertEquals(0, tokens.size());
    }

    @Test
    public void generateTokenWIthUpdateStatementTest() {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final UpdateStatement updateStatement = mock(UpdateStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ExpressionSegment expressionSegment = mock(ExpressionSegment.class);

        when(updateStatementContext.getSqlStatement()).thenReturn(updateStatement);
        when(updateStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(updateStatementContext.getSchemaName()).thenReturn("schema");
        when(updateStatement.getSetAssignment()).thenReturn(setAssignmentSegment);
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(assignmentSegment.getValue()).thenReturn(expressionSegment);


        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> tokens = tokenGenerator.generateSQLTokens(updateStatementContext);
        assertEquals(0, tokens.size());
    }
}
