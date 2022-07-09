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

import java.util.Collections;
import java.util.Optional;

import org.apache.shardingsphere.encrypt.rewrite.token.generator.AssistQueryAndPlainInsertColumnsTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssistQueryAndPlainInsertColumnsTokenGeneratorTest {
    
    private final AssistQueryAndPlainInsertColumnsTokenGenerator tokenGenerator = new AssistQueryAndPlainInsertColumnsTokenGenerator();
    
    private InsertStatementContext insertStatementContext;
    
    @Before
    public void setup() {
        final String tableName = "foo_tbl";
        final String columnName = "foo_col";
        insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        EncryptRule encryptRule = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        tokenGenerator.setEncryptRule(encryptRule);
        when(insertStatementContext.getSqlStatement()
                .getInsertColumns()).thenReturn(Optional.of(mock(InsertColumnsSegment.class)));
        when(insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier()
                .getValue()).thenReturn(tableName);
        ColumnSegment columnSegment = mock(ColumnSegment.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement()
                .getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getIdentifier().getValue()).thenReturn(columnName);
        EncryptTable encryptTable = mock(EncryptTable.class, RETURNS_DEEP_STUBS);
        when(encryptRule.findEncryptTable(tableName)).thenReturn(Optional.of(encryptTable));
        when(encryptTable.findAssistedQueryColumn(columnName)).thenReturn(Optional.of("assisted_query_col"));
        when(encryptTable.findPlainColumn(columnName)).thenReturn(Optional.of("plain_col"));
    }
    
    @Test
    public void assertIsGenerateSQLToken() {
        assertTrue(tokenGenerator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    public void assertGenerateSQLTokens() {
        assertThat(tokenGenerator.generateSQLTokens(insertStatementContext).size(), is(1));
    }
}
