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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptColumnItem;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptCreateTableTokenGeneratorTest {
    
    private final EncryptCreateTableTokenGenerator generator = new EncryptCreateTableTokenGenerator();
    
    @BeforeEach
    void setup() {
        generator.setEncryptRule(mockEncryptRule());
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mockEncryptTable();
        when(result.getEncryptTable("t_encrypt")).thenReturn(encryptTable);
        return result;
    }
    
    private EncryptTable mockEncryptTable() {
        EncryptTable result = mock(EncryptTable.class);
        EncryptColumn column = mockEncryptColumn();
        when(result.getLogicColumns()).thenReturn(Collections.singletonList("t_encrypt"));
        when(result.isEncryptColumn("certificate_number")).thenReturn(true);
        when(result.getCipherColumn("certificate_number")).thenReturn(column.getCipher().getName());
        when(result.findAssistedQueryColumn("certificate_number")).thenReturn(column.getAssistedQuery().map(EncryptColumnItem::getName));
        when(result.findLikeQueryColumn("certificate_number")).thenReturn(column.getLikeQuery().map(EncryptColumnItem::getName));
        when(result.findEncryptColumn("certificate_number")).thenReturn(Optional.of(column));
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        EncryptColumn result = new EncryptColumn("certificate_number", new EncryptColumnItem("cipher_certificate_number", "test"));
        result.setAssistedQuery(new EncryptColumnItem("assisted_certificate_number", "assisted_encryptor"));
        result.setLikeQuery(new EncryptColumnItem("like_certificate_number", "like_encryptor"));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mockCreateTableStatementContext());
        assertThat(actual.size(), is(4));
        Iterator<SQLToken> actualIterator = actual.iterator();
        assertThat(actualIterator.next(), instanceOf(RemoveToken.class));
        SubstitutableColumnNameToken cipherToken = (SubstitutableColumnNameToken) actualIterator.next();
        assertThat(cipherToken.toString(mock(RouteUnit.class)), is("cipher_certificate_number"));
        assertThat(cipherToken.getStartIndex(), is(79));
        assertThat(cipherToken.getStopIndex(), is(42));
        SubstitutableColumnNameToken assistedToken = (SubstitutableColumnNameToken) actualIterator.next();
        assertThat(assistedToken.toString(mock(RouteUnit.class)), is(", assisted_certificate_number"));
        assertThat(assistedToken.getStartIndex(), is(79));
        assertThat(assistedToken.getStopIndex(), is(42));
        SubstitutableColumnNameToken likeToken = (SubstitutableColumnNameToken) actualIterator.next();
        assertThat(likeToken.toString(mock(RouteUnit.class)), is(", like_certificate_number"));
        assertThat(likeToken.getStartIndex(), is(79));
        assertThat(likeToken.getStopIndex(), is(42));
    }
    
    private CreateTableStatementContext mockCreateTableStatementContext() {
        CreateTableStatementContext result = mock(CreateTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(25, 78, new ColumnSegment(25, 42, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        when(result.getSqlStatement().getColumnDefinitions()).thenReturn(Collections.singletonList(segment));
        return result;
    }
}
