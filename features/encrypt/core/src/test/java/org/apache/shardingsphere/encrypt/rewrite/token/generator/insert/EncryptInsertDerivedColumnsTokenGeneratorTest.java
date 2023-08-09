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

import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptInsertDerivedColumnsTokenGeneratorTest {
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotInsertStatementContext() {
        assertFalse(new EncryptInsertDerivedColumnsTokenGenerator().isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutInsertColumns() {
        assertFalse(new EncryptInsertDerivedColumnsTokenGenerator().isGenerateSQLToken(mock(InsertStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithInsertColumns() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.containsInsertColumns()).thenReturn(true);
        assertTrue(new EncryptInsertDerivedColumnsTokenGenerator().isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokensNotContainColumns() {
        EncryptInsertDerivedColumnsTokenGenerator tokenGenerator = new EncryptInsertDerivedColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(mockEncryptRule());
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("foo_tbl");
        assertTrue(tokenGenerator.generateSQLTokens(insertStatementContext).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensNotExistColumns() {
        EncryptInsertDerivedColumnsTokenGenerator tokenGenerator = new EncryptInsertDerivedColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(mockEncryptRule());
        ColumnSegment columnSegment = mock(ColumnSegment.class, RETURNS_DEEP_STUBS);
        when(columnSegment.getIdentifier().getValue()).thenReturn("bar_col");
        InsertStatementContext insertStatementContext = mockInsertStatementContext();
        when(insertStatementContext.getSqlStatement().getColumns()).thenReturn(Collections.singleton(columnSegment));
        assertTrue(tokenGenerator.generateSQLTokens(insertStatementContext).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensExistColumns() {
        EncryptInsertDerivedColumnsTokenGenerator tokenGenerator = new EncryptInsertDerivedColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(mockEncryptRule());
        Collection<SQLToken> actual = tokenGenerator.generateSQLTokens(mockInsertStatementContext());
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getStartIndex(), is(1));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class, RETURNS_DEEP_STUBS);
        when(encryptTable.isEncryptColumn("foo_col")).thenReturn(true);
        when(encryptTable.getEncryptColumn("foo_col").getAssistedQuery()).thenReturn(Optional.of(new AssistedQueryColumnItem("assisted_query_col", mock(AssistedEncryptAlgorithm.class))));
        when(result.getEncryptTable("foo_tbl")).thenReturn(encryptTable);
        return result;
    }
    
    private InsertStatementContext mockInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("foo_tbl");
        ColumnSegment columnSegment = mock(ColumnSegment.class, RETURNS_DEEP_STUBS);
        when(columnSegment.getIdentifier().getValue()).thenReturn("foo_col");
        when(result.getSqlStatement().getColumns()).thenReturn(Collections.singleton(columnSegment));
        return result;
    }
}
