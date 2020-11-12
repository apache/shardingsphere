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

package org.apache.shardingsphere.encrypt.merge.dal;

import org.apache.shardingsphere.encrypt.merge.dal.impl.MergedEncryptColumnsMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MergedEncryptColumnsMergedResultTest {
    
    @Mock
    private QueryResult queryResult;
    
    @Test
    public void assertNextWithNotHasNext() throws SQLException {
        assertFalse(createMergedEncryptColumnsMergedResult(queryResult, mock(EncryptRule.class)).next());
    }
    
    @Test
    public void assertNextWithHasNext() throws SQLException {
        when(queryResult.next()).thenReturn(true);
        assertTrue(createMergedEncryptColumnsMergedResult(queryResult, mock(EncryptRule.class)).next());
    }
    
    @Test
    public void assertNextWithAssistedQuery() throws SQLException {
        when(queryResult.next()).thenReturn(true).thenReturn(false);
        when(queryResult.getValue(1, String.class)).thenReturn("assistedQuery");
        assertFalse(createMergedEncryptColumnsMergedResult(queryResult, mockEncryptRule()).next());
    }
    
    @Test
    public void assertGetValueWithCipherColumn() throws SQLException {
        when(queryResult.getValue(1, String.class)).thenReturn("cipher");
        assertThat(createMergedEncryptColumnsMergedResult(queryResult, mockEncryptRule()).getValue(1, String.class), is("id"));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptRule.findEncryptTable("test")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.getAssistedQueryColumns()).thenReturn(Collections.singleton("assistedQuery"));
        when(encryptTable.isCipherColumn("cipher")).thenReturn(true);
        when(encryptTable.getLogicColumn("cipher")).thenReturn("id");
        return encryptRule;
    }
    
    @Test
    public void assertGetValueWithOtherColumn() throws SQLException {
        when(queryResult.getValue(1, String.class)).thenReturn("assistedQuery");
        assertThat(createMergedEncryptColumnsMergedResult(queryResult, mock(EncryptRule.class)).getValue(1, String.class), is("assistedQuery"));
    }
    
    @Test
    public void assertGetValueWithOtherIndex() throws SQLException {
        when(queryResult.getValue(2, String.class)).thenReturn("id");
        assertThat(createMergedEncryptColumnsMergedResult(queryResult, mock(EncryptRule.class)).getValue(2, String.class), is("id"));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        assertFalse(createMergedEncryptColumnsMergedResult(queryResult, mock(EncryptRule.class)).wasNull());
    }
    
    private MergedEncryptColumnsMergedResult createMergedEncryptColumnsMergedResult(final QueryResult queryResult, final EncryptRule encryptRule) {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        IdentifierValue identifierValue = new IdentifierValue("test");
        TableNameSegment tableNameSegment = new TableNameSegment(1, 4, identifierValue);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        when(sqlStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        return new MergedEncryptColumnsMergedResult(queryResult, sqlStatementContext, encryptRule);
    }
}
