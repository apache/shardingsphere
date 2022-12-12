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

import org.apache.shardingsphere.encrypt.rewrite.token.generator.AssistQueryAndPlainInsertColumnsTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertColumnsToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AssistQueryAndPlainInsertColumnsTokenGeneratorTest {
    
    @Test
    public void assertIsNotGenerateSQLTokenWithNotInsertStatementContext() {
        assertFalse(new AssistQueryAndPlainInsertColumnsTokenGenerator().isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    public void assertIsNotGenerateSQLTokenWithoutInsertColumns() {
        assertFalse(new AssistQueryAndPlainInsertColumnsTokenGenerator().isGenerateSQLToken(mock(InsertStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    public void assertIsGenerateSQLTokenWithInsertColumns() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.containsInsertColumns()).thenReturn(true);
        assertTrue(new AssistQueryAndPlainInsertColumnsTokenGenerator().isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    public void assertGenerateSQLTokensNotContainColumns() {
        AssistQueryAndPlainInsertColumnsTokenGenerator tokenGenerator = new AssistQueryAndPlainInsertColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(mockEncryptRule());
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("foo_tbl");
        assertTrue(tokenGenerator.generateSQLTokens(insertStatementContext).isEmpty());
    }
    
    @Test
    public void assertGenerateSQLTokensNotExistColumns() {
        AssistQueryAndPlainInsertColumnsTokenGenerator tokenGenerator = new AssistQueryAndPlainInsertColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(mockEncryptRule());
        ColumnSegment columnSegment = mock(ColumnSegment.class, RETURNS_DEEP_STUBS);
        when(columnSegment.getIdentifier().getValue()).thenReturn("bar_col");
        InsertStatementContext insertStatementContext = mockInsertStatementContext();
        when(insertStatementContext.getSqlStatement().getColumns()).thenReturn(Collections.singleton(columnSegment));
        assertTrue(tokenGenerator.generateSQLTokens(insertStatementContext).isEmpty());
    }
    
    @Test
    public void assertGenerateSQLTokensExistColumns() {
        AssistQueryAndPlainInsertColumnsTokenGenerator tokenGenerator = new AssistQueryAndPlainInsertColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(mockEncryptRule());
        Collection<InsertColumnsToken> actual = tokenGenerator.generateSQLTokens(mockInsertStatementContext());
        assertThat(actual.size(), is(1));
        Iterator<InsertColumnsToken> iterator = actual.iterator();
        InsertColumnsToken insertColumnsToken = iterator.next();
        assertThat(insertColumnsToken.getStartIndex(), is(1));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.findAssistedQueryColumn("foo_col")).thenReturn(Optional.of("assisted_query_col"));
        when(encryptTable.findPlainColumn("foo_col")).thenReturn(Optional.of("plain_col"));
        when(result.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
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
