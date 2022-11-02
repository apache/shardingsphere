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

package org.apache.shardingsphere.encrypt.rewrite.impl;

import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptAlterTableTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAlterTableToken;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
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
import org.junit.Before;
import org.junit.Test;

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

public final class EncryptAlterTableTokenGeneratorTest {
    
    private EncryptAlterTableTokenGenerator generator;
    
    @Before
    public void setup() {
        generator = new EncryptAlterTableTokenGenerator();
        generator.setEncryptRule(mockEncryptRule());
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        when(result.getCipherColumn("t_encrypt", "certificate_number")).thenReturn("cipher_certificate_number");
        when(result.findAssistedQueryColumn("t_encrypt", "certificate_number")).thenReturn(Optional.of("assisted_certificate_number"));
        when(result.findPlainColumn("t_encrypt", "certificate_number")).thenReturn(Optional.of("certificate_number_plain"));
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.getLogicColumns()).thenReturn(Collections.singleton("t_encrypt"));
        EncryptAlgorithm<?, ?> encryptAlgorithm = mock(EncryptAlgorithm.class);
        when(result.findEncryptor("t_encrypt", "certificate_number")).thenReturn(Optional.of(encryptAlgorithm));
        when(result.findEncryptor("t_encrypt", "certificate_number_new")).thenReturn(Optional.of(encryptAlgorithm));
        when(result.findEncryptTable("t_encrypt")).thenReturn(Optional.of(encryptTable));
        when(result.findEncryptColumn("t_encrypt", "certificate_number")).thenReturn(Optional.of(mockEncryptColumn()));
        when(result.findEncryptColumn("t_encrypt", "certificate_number_new")).thenReturn(Optional.of(mockNewEncryptColumn()));
        when(result.getCipherColumn("t_encrypt", "certificate_number_new")).thenReturn("cipher_certificate_number_new");
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        return new EncryptColumn("cipher_certificate_number", "assisted_certificate_number", "fuzzy_certificate_number", "certificate_number_plain", "test", null);
    }
    
    private EncryptColumn mockNewEncryptColumn() {
        return new EncryptColumn("cipher_certificate_number_new", "assisted_certificate_number_new", "fuzzy_certificate_number_new", "certificate_number_new_plain", "test", null);
    }
    
    @Test
    public void assertAddColumnGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildAddColumnStatementContext());
        assertThat(sqlTokens.size(), is(4));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertThat(iterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken cipherToken = (EncryptAlterTableToken) iterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number"));
        assertThat(cipherToken.getStartIndex(), is(51));
        assertThat(cipherToken.getStopIndex(), is(50));
        EncryptAlterTableToken assistedToken = (EncryptAlterTableToken) iterator.next();
        assertThat(assistedToken.toString(), is(", ADD COLUMN assisted_certificate_number"));
        assertThat(assistedToken.getStartIndex(), is(68));
        assertThat(assistedToken.getStopIndex(), is(50));
        EncryptAlterTableToken plainToken = (EncryptAlterTableToken) iterator.next();
        assertThat(plainToken.toString(), is(", ADD COLUMN certificate_number_plain"));
        assertThat(plainToken.getStartIndex(), is(68));
        assertThat(plainToken.getStopIndex(), is(50));
    }
    
    private AlterTableStatementContext buildAddColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(33, 67, new ColumnSegment(33, 50, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        AddColumnDefinitionSegment addColumnDefinitionSegment = new AddColumnDefinitionSegment(22, 67, Collections.singletonList(segment));
        when(result.getSqlStatement().getAddColumnDefinitions()).thenReturn(Collections.singletonList(addColumnDefinitionSegment));
        return result;
    }
    
    @Test
    public void assertModifyColumnGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildModifyColumnStatementContext());
        assertThat(sqlTokens.size(), is(4));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertThat(iterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken cipherToken = (EncryptAlterTableToken) iterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number"));
        assertThat(cipherToken.getStartIndex(), is(54));
        assertThat(cipherToken.getStopIndex(), is(53));
        EncryptAlterTableToken assistedToken = (EncryptAlterTableToken) iterator.next();
        assertThat(assistedToken.toString(), is(", MODIFY COLUMN assisted_certificate_number"));
        assertThat(assistedToken.getStartIndex(), is(71));
        assertThat(assistedToken.getStopIndex(), is(53));
        EncryptAlterTableToken plainToken = (EncryptAlterTableToken) iterator.next();
        assertThat(plainToken.toString(), is(", MODIFY COLUMN certificate_number_plain"));
        assertThat(plainToken.getStartIndex(), is(71));
        assertThat(plainToken.getStopIndex(), is(53));
    }
    
    private AlterTableStatementContext buildModifyColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(36, 70, new ColumnSegment(36, 53, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        ModifyColumnDefinitionSegment modifyColumnDefinitionSegment = new ModifyColumnDefinitionSegment(22, 70, segment);
        when(result.getSqlStatement().getModifyColumnDefinitions()).thenReturn(Collections.singletonList(modifyColumnDefinitionSegment));
        return result;
    }
    
    @Test
    public void assertChangeColumnGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildChangeColumnStatementContext());
        assertThat(sqlTokens.size(), is(6));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertThat(iterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken previous = (EncryptAlterTableToken) iterator.next();
        assertThat(previous.toString(), is("cipher_certificate_number"));
        assertThat(iterator.next(), instanceOf(RemoveToken.class));
        EncryptAlterTableToken cipherToken = (EncryptAlterTableToken) iterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number_new"));
        assertThat(cipherToken.getStartIndex(), is(77));
        assertThat(cipherToken.getStopIndex(), is(76));
        EncryptAlterTableToken assistedToken = (EncryptAlterTableToken) iterator.next();
        assertThat(assistedToken.toString(), is(", CHANGE COLUMN assisted_certificate_number assisted_certificate_number_new"));
        assertThat(assistedToken.getStartIndex(), is(94));
        assertThat(assistedToken.getStopIndex(), is(76));
        EncryptAlterTableToken plainToken = (EncryptAlterTableToken) iterator.next();
        assertThat(plainToken.toString(), is(", CHANGE COLUMN certificate_number_plain certificate_number_new_plain"));
        assertThat(plainToken.getStartIndex(), is(94));
        assertThat(plainToken.getStopIndex(), is(76));
    }
    
    private AlterTableStatementContext buildChangeColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(55, 93, new ColumnSegment(55, 76, new IdentifierValue("certificate_number_new")), new DataTypeSegment(), false, false);
        ChangeColumnDefinitionSegment changeColumnDefinitionSegment = new ChangeColumnDefinitionSegment(22, 93, segment);
        changeColumnDefinitionSegment.setPreviousColumn(new ColumnSegment(36, 53, new IdentifierValue("certificate_number")));
        when(result.getSqlStatement().getChangeColumnDefinitions()).thenReturn(Collections.singletonList(changeColumnDefinitionSegment));
        return result;
    }
}
