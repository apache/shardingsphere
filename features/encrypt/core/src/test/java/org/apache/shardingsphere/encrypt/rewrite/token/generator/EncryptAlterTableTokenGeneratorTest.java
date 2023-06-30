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

import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.like.LikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAlterTableToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
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

class EncryptAlterTableTokenGeneratorTest {
    
    private final EncryptAlterTableTokenGenerator generator = new EncryptAlterTableTokenGenerator();
    
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
        when(result.getTable()).thenReturn("t_encrypt");
        when(result.isEncryptColumn("certificate_number")).thenReturn(true);
        when(result.getEncryptColumn("certificate_number")).thenReturn(mockEncryptColumn());
        when(result.isEncryptColumn("certificate_number_new")).thenReturn(true);
        when(result.getEncryptColumn("certificate_number_new")).thenReturn(mockNewEncryptColumn());
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        EncryptColumn result = new EncryptColumn("certificate_number", new CipherColumnItem("cipher_certificate_number", mock(StandardEncryptAlgorithm.class)));
        result.setAssistedQuery(new AssistedQueryColumnItem("assisted_certificate_number", mock(AssistedEncryptAlgorithm.class)));
        result.setLikeQuery(new LikeQueryColumnItem("like_certificate_number", mock(LikeEncryptAlgorithm.class)));
        return result;
    }
    
    private EncryptColumn mockNewEncryptColumn() {
        EncryptColumn result = new EncryptColumn(
                "certificate_number_new", new CipherColumnItem("cipher_certificate_number_new", mock(StandardEncryptAlgorithm.class)));
        result.setAssistedQuery(new AssistedQueryColumnItem("assisted_certificate_number_new", mock(AssistedEncryptAlgorithm.class)));
        result.setLikeQuery(new LikeQueryColumnItem("like_certificate_number_new", mock(LikeEncryptAlgorithm.class)));
        return result;
    }
    
    @Test
    void assertAddColumnGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mockAddColumnStatementContext());
        assertThat(actual.size(), is(4));
        Iterator<SQLToken> actualIterator = actual.iterator();
        assertThat(actualIterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken cipherToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number"));
        assertThat(cipherToken.getStartIndex(), is(51));
        assertThat(cipherToken.getStopIndex(), is(50));
        EncryptAlterTableToken assistedToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(assistedToken.toString(), is(", ADD COLUMN assisted_certificate_number"));
        assertThat(assistedToken.getStartIndex(), is(68));
        assertThat(assistedToken.getStopIndex(), is(50));
        EncryptAlterTableToken likeToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(likeToken.toString(), is(", ADD COLUMN like_certificate_number"));
        assertThat(likeToken.getStartIndex(), is(68));
        assertThat(likeToken.getStopIndex(), is(50));
    }
    
    private AlterTableStatementContext mockAddColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment columnDefinitionSegment = new ColumnDefinitionSegment(
                33, 67, new ColumnSegment(33, 50, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        when(result.getSqlStatement().getAddColumnDefinitions()).thenReturn(Collections.singleton(new AddColumnDefinitionSegment(22, 67, Collections.singleton(columnDefinitionSegment))));
        return result;
    }
    
    @Test
    void assertModifyColumnGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mockModifyColumnStatementContext());
        assertThat(actual.size(), is(4));
        Iterator<SQLToken> actualIterator = actual.iterator();
        assertThat(actualIterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken cipherToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number"));
        assertThat(cipherToken.getStartIndex(), is(54));
        assertThat(cipherToken.getStopIndex(), is(53));
        EncryptAlterTableToken assistedToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(assistedToken.toString(), is(", MODIFY COLUMN assisted_certificate_number"));
        assertThat(assistedToken.getStartIndex(), is(71));
        assertThat(assistedToken.getStopIndex(), is(53));
        EncryptAlterTableToken likeToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(likeToken.toString(), is(", MODIFY COLUMN like_certificate_number"));
        assertThat(likeToken.getStartIndex(), is(71));
        assertThat(likeToken.getStopIndex(), is(53));
    }
    
    private AlterTableStatementContext mockModifyColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment columnDefinitionSegment = new ColumnDefinitionSegment(
                36, 70, new ColumnSegment(36, 53, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        when(result.getSqlStatement().getModifyColumnDefinitions()).thenReturn(Collections.singleton(new ModifyColumnDefinitionSegment(22, 70, columnDefinitionSegment)));
        return result;
    }
    
    @Test
    void assertChangeColumnGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mockChangeColumnStatementContext());
        assertThat(actual.size(), is(6));
        Iterator<SQLToken> actualIterator = actual.iterator();
        assertThat(actualIterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken previous = (EncryptAlterTableToken) actualIterator.next();
        assertThat(previous.toString(), is("cipher_certificate_number"));
        assertThat(actualIterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken cipherToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number_new"));
        assertThat(cipherToken.getStartIndex(), is(77));
        assertThat(cipherToken.getStopIndex(), is(76));
        EncryptAlterTableToken assistedToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(assistedToken.toString(), is(", CHANGE COLUMN assisted_certificate_number assisted_certificate_number_new"));
        assertThat(assistedToken.getStartIndex(), is(94));
        assertThat(assistedToken.getStopIndex(), is(76));
        EncryptAlterTableToken likeToken = (EncryptAlterTableToken) actualIterator.next();
        assertThat(likeToken.toString(), is(", CHANGE COLUMN like_certificate_number like_certificate_number_new"));
        assertThat(likeToken.getStartIndex(), is(94));
        assertThat(likeToken.getStopIndex(), is(76));
    }
    
    private AlterTableStatementContext mockChangeColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment columnDefinitionSegment = new ColumnDefinitionSegment(
                55, 93, new ColumnSegment(55, 76, new IdentifierValue("certificate_number_new")), new DataTypeSegment(), false, false);
        ChangeColumnDefinitionSegment changeColumnDefinitionSegment = new ChangeColumnDefinitionSegment(22, 93, columnDefinitionSegment);
        changeColumnDefinitionSegment.setPreviousColumn(new ColumnSegment(36, 53, new IdentifierValue("certificate_number")));
        when(result.getSqlStatement().getChangeColumnDefinitions()).thenReturn(Collections.singleton(changeColumnDefinitionSegment));
        return result;
    }
}
