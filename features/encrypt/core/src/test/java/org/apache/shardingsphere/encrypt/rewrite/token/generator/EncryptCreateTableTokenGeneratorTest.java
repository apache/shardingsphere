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

import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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
        EncryptTable result = mock(EncryptTable.class, RETURNS_DEEP_STUBS);
        EncryptColumn encryptColumn = mockEncryptColumn();
        when(result.isEncryptColumn("certificate_number")).thenReturn(true);
        when(result.getEncryptColumn("certificate_number")).thenReturn(encryptColumn);
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        EncryptColumn result = new EncryptColumn("certificate_number", new CipherColumnItem("cipher_certificate_number", mock(EncryptAlgorithm.class)));
        result.setAssistedQuery(new AssistedQueryColumnItem("assisted_certificate_number", mock(EncryptAlgorithm.class)));
        result.setLikeQuery(new LikeQueryColumnItem("like_certificate_number", mock(EncryptAlgorithm.class)));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mockCreateTableStatementContext());
        assertThat(actual.size(), is(4));
        Iterator<SQLToken> actualIterator = actual.iterator();
        assertThat(actualIterator.next(), instanceOf(RemoveToken.class));
        EncryptColumnToken cipherToken = (EncryptColumnToken) actualIterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number VARCHAR(4000)"));
        assertThat(cipherToken.getStartIndex(), is(79));
        assertThat(cipherToken.getStopIndex(), is(78));
        EncryptColumnToken assistedToken = (EncryptColumnToken) actualIterator.next();
        assertThat(assistedToken.toString(), is(", assisted_certificate_number VARCHAR(4000)"));
        assertThat(assistedToken.getStartIndex(), is(79));
        assertThat(assistedToken.getStopIndex(), is(78));
        EncryptColumnToken likeToken = (EncryptColumnToken) actualIterator.next();
        assertThat(likeToken.toString(), is(", like_certificate_number VARCHAR(4000)"));
        assertThat(likeToken.getStartIndex(), is(79));
        assertThat(likeToken.getStopIndex(), is(78));
    }
    
    private CreateTableStatementContext mockCreateTableStatementContext() {
        CreateTableStatementContext result = mock(CreateTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(25, 78, new ColumnSegment(25, 42, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        when(result.getSqlStatement().getColumnDefinitions()).thenReturn(Collections.singleton(segment));
        return result;
    }
}
