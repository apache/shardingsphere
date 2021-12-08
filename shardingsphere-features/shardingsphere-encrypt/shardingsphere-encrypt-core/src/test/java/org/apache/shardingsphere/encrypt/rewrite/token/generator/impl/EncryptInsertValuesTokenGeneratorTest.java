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

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptInsertValuesTokenGeneratorTest {

    @InjectMocks
    private EncryptInsertValuesTokenGenerator tokenGenerator;

    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertValuesSegment insertValuesSegment = mock(InsertValuesSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getValues()).thenReturn(Collections.singletonList(insertValuesSegment));

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext);
        assertTrue(actual);
    }

    @Test
    public void generateSQLTokenHavingPreviousTokensTest() {
        IdentifierValue idf = new IdentifierValue("table1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertValuesToken insertValuesToken = mock(InsertValuesToken.class);
        final UseDefaultInsertColumnsToken useDefaultInsertColumnsToken = mock(UseDefaultInsertColumnsToken.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final InsertValueContext insertValueContext = mock(InsertValueContext.class);
        final InsertValue insertValue = mock(InsertValue.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final QueryAssistedEncryptAlgorithm encryptAlgorithm = mock(QueryAssistedEncryptAlgorithm.class);
        final LiteralExpressionSegment expressionSegment = mock(LiteralExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(insertValuesToken.getInsertValues()).thenReturn(Collections.singletonList(insertValue));
        when(insertStatementContext.getDescendingColumnNames()).thenReturn(Collections.singletonList("col1").iterator());
        when(useDefaultInsertColumnsToken.getColumns()).thenReturn(Collections.singletonList("col1"));
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertValueContext.getValue(anyInt())).thenReturn(new Object());
        when(insertValueContext.getParameters()).thenReturn(Collections.emptyList());
        when(insertValue.getValues()).thenReturn(new ArrayList<>(Arrays.asList(expressionSegment)));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("pCol"));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("aqCol"));
        when(insertValueContext.getValueExpressions()).thenReturn(Collections.singletonList(expressionSegment));

        tokenGenerator.setPreviousSQLTokens(Arrays.asList(insertValuesToken, useDefaultInsertColumnsToken));
        tokenGenerator.setEncryptRule(encryptRule);

        final InsertValuesToken previousToken = tokenGenerator.generateSQLToken(insertStatementContext);
        assertNotNull(previousToken);
        assertNotNull(previousToken.getInsertValues());
        assertEquals(1, previousToken.getInsertValues().size());
    }

    @Test
    public void generateSQLTokenWithNoPreviousTokensTest() {
        IdentifierValue idf = new IdentifierValue("table1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final InsertValueContext insertValueContext = mock(InsertValueContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final QueryAssistedEncryptAlgorithm encryptAlgorithm = mock(QueryAssistedEncryptAlgorithm.class);
        final LiteralExpressionSegment expressionSegment = mock(LiteralExpressionSegment.class);
        final InsertValuesSegment insertValuesSegment = mock(InsertValuesSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("col1"));
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatement.getValues()).thenReturn(Collections.singletonList(insertValuesSegment));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(insertStatementContext.getDescendingColumnNames()).thenReturn(Collections.singletonList("col1").iterator());
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(insertStatementContext.getSchemaName()).thenReturn("schema");
        when(insertValueContext.getValue(anyInt())).thenReturn(new Object());
        when(insertValueContext.getParameters()).thenReturn(Collections.emptyList());
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("pCol"));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("aqCol"));
        when(insertValueContext.getValueExpressions()).thenReturn(new ArrayList<>(Arrays.asList(expressionSegment)));

        tokenGenerator.setPreviousSQLTokens(new ArrayList<>());
        tokenGenerator.setEncryptRule(encryptRule);

        final InsertValuesToken previousToken = tokenGenerator.generateSQLToken(insertStatementContext);
        assertNotNull(previousToken);
        assertNotNull(previousToken.getInsertValues());
        assertEquals(1, previousToken.getInsertValues().size());
    }
}
