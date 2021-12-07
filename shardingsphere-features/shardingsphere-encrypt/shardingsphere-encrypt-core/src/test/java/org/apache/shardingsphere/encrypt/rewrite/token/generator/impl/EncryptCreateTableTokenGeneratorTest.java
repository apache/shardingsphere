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
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptCreateTableTokenGeneratorTest {

    @InjectMocks
    private EncryptCreateTableTokenGenerator tokenGenerator;
    
    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        CreateTableStatementContext createTableStatementContext = mock(CreateTableStatementContext.class);
        CreateTableStatement createTableStatement = mock(CreateTableStatement.class);
        ColumnDefinitionSegment columnDefinitionSegment = mock(ColumnDefinitionSegment.class);

        when(createTableStatementContext.getSqlStatement()).thenReturn(createTableStatement);
        when(createTableStatement.getColumnDefinitions()).thenReturn(Collections.singleton(columnDefinitionSegment));

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(createTableStatementContext);
        assertTrue(actual);
    }

    @Test
    public void generateSQLTokensTest() {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        CreateTableStatementContext createTableStatementContext = mock(CreateTableStatementContext.class);
        CreateTableStatement createTableStatement = mock(CreateTableStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        ColumnDefinitionSegment columnDefinitionSegment = mock(ColumnDefinitionSegment.class);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);

        when(createTableStatementContext.getSqlStatement()).thenReturn(createTableStatement);
        when(createTableStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(createTableStatement.getColumnDefinitions()).thenReturn(Collections.singleton(columnDefinitionSegment));
        when(columnDefinitionSegment.getColumnName()).thenReturn(columnSegment);
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptRule.findEncryptor(anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.getCipherColumn(anyString(), anyString())).thenReturn("col1");
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("aqColumn"));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("fpColumn"));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SQLToken> sqlTokens = tokenGenerator.generateSQLTokens(createTableStatementContext);
        assertNotNull(sqlTokens);
        assertEquals(4, sqlTokens.size());
    }
}
