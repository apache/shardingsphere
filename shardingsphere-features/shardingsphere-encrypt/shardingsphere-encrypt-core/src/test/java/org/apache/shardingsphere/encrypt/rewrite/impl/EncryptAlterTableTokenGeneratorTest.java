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
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptConfigDataTypeToken;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptColumnDataType;
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

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptAlterTableTokenGeneratorTest {
    
    private EncryptAlterTableTokenGenerator generator;
    
    @Before
    public void setup() {
        generator = new EncryptAlterTableTokenGenerator();
        generator.setEncryptRule(buildEncryptRule());
    }
    
    @Test
    public void assertAddColumnGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildAddColumnStatementContext());
        assertThat(sqlTokens.size(), is(4));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertTrue(iterator.next() instanceof RemoveToken);
        EncryptConfigDataTypeToken cipherToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number varchar(200) not null default ''"));
        assertThat(cipherToken.getStartIndex(), is(68));
        assertThat(cipherToken.getStopIndex(), is(67));
        EncryptConfigDataTypeToken assistedToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(assistedToken.toString(), is(", ADD COLUMN assisted_certificate_number varchar(200) not null"));
        assertThat(assistedToken.getStartIndex(), is(68));
        assertThat(assistedToken.getStopIndex(), is(67));
        EncryptConfigDataTypeToken plainToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(plainToken.toString(), is(", ADD COLUMN certificate_number_plain int(20) unsigned not null default 0"));
        assertThat(plainToken.getStartIndex(), is(68));
        assertThat(plainToken.getStopIndex(), is(67));
    }
    
    @Test
    public void assertModifyColumnGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildModifyColumnStatementContext());
        assertThat(sqlTokens.size(), is(4));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertTrue(iterator.next() instanceof RemoveToken);
        EncryptConfigDataTypeToken cipherToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number varchar(200) not null default ''"));
        assertThat(cipherToken.getStartIndex(), is(71));
        assertThat(cipherToken.getStopIndex(), is(70));
        EncryptConfigDataTypeToken assistedToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(assistedToken.toString(), is(", MODIFY COLUMN assisted_certificate_number varchar(200) not null"));
        assertThat(assistedToken.getStartIndex(), is(71));
        assertThat(assistedToken.getStopIndex(), is(70));
        EncryptConfigDataTypeToken plainToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(plainToken.toString(), is(", MODIFY COLUMN certificate_number_plain int(20) unsigned not null default 0"));
        assertThat(plainToken.getStartIndex(), is(71));
        assertThat(plainToken.getStopIndex(), is(70));
    }
    
    @Test
    public void assertChangeColumnGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildChangeColumnStatementContext());
        assertThat(sqlTokens.size(), is(6));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertTrue(iterator.next() instanceof RemoveToken);
        EncryptAlterTableToken previous = (EncryptAlterTableToken) iterator.next();
        assertThat(previous.toString(), is("cipher_certificate_number"));
        assertTrue(iterator.next() instanceof RemoveToken);
        EncryptConfigDataTypeToken cipherToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number_new varchar(500) not null"));
        assertThat(cipherToken.getStartIndex(), is(94));
        assertThat(cipherToken.getStopIndex(), is(93));
        EncryptConfigDataTypeToken assistedToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(assistedToken.toString(), is(", CHANGE COLUMN assisted_certificate_number assisted_certificate_number_new varchar(200) not null"));
        assertThat(assistedToken.getStartIndex(), is(94));
        assertThat(assistedToken.getStopIndex(), is(93));
        EncryptConfigDataTypeToken plainToken = (EncryptConfigDataTypeToken) iterator.next();
        assertThat(plainToken.toString(), is(", CHANGE COLUMN certificate_number_plain certificate_number_new_plain int(20) unsigned not null default 0"));
        assertThat(plainToken.getStartIndex(), is(94));
        assertThat(plainToken.getStopIndex(), is(93));
    }
    
    private AlterTableStatementContext buildAddColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(33, 67,
                new ColumnSegment(33, 50, new IdentifierValue("certificate_number")), new DataTypeSegment(), false);
        AddColumnDefinitionSegment addColumnDefinitionSegment = new AddColumnDefinitionSegment(22, 67, Collections.singletonList(segment));
        when(result.getSqlStatement().getAddColumnDefinitions()).thenReturn(Collections.singletonList(addColumnDefinitionSegment));
        return result;
    }

    private AlterTableStatementContext buildModifyColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(36, 70,
                new ColumnSegment(36, 53, new IdentifierValue("certificate_number")), new DataTypeSegment(), false);
        ModifyColumnDefinitionSegment modifyColumnDefinitionSegment = new ModifyColumnDefinitionSegment(22, 70, segment);
        when(result.getSqlStatement().getModifyColumnDefinitions()).thenReturn(Collections.singletonList(modifyColumnDefinitionSegment));
        return result;
    }
    
    private AlterTableStatementContext buildChangeColumnStatementContext() {
        AlterTableStatementContext result = mock(AlterTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(55, 93,
                new ColumnSegment(55, 76, new IdentifierValue("certificate_number_new")), new DataTypeSegment(), false);
        ChangeColumnDefinitionSegment changeColumnDefinitionSegment = new ChangeColumnDefinitionSegment(22, 93, segment);
        changeColumnDefinitionSegment.setPreviousColumn(new ColumnSegment(36, 53, new IdentifierValue("certificate_number")));
        when(result.getSqlStatement().getChangeColumnDefinitions()).thenReturn(Collections.singletonList(changeColumnDefinitionSegment));
        return result;
    }
    
    private EncryptRule buildEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.getCipherColumn("t_encrypt", "certificate_number")).thenReturn("cipher_certificate_number");
        when(result.findAssistedQueryColumn("t_encrypt", "certificate_number")).thenReturn(Optional.of("assisted_certificate_number"));
        when(result.findPlainColumn("t_encrypt", "certificate_number")).thenReturn(Optional.of("certificate_number_plain"));
        when(encryptTable.getLogicColumns()).thenReturn(Collections.singletonList("t_encrypt"));
        EncryptAlgorithm<?, ?> encryptAlgorithm = mock(EncryptAlgorithm.class);
        when(result.findEncryptor("t_encrypt", "certificate_number")).thenReturn(Optional.of(encryptAlgorithm));
        when(result.findEncryptor("t_encrypt", "certificate_number_new")).thenReturn(Optional.of(encryptAlgorithm));
        when(result.findEncryptTable("t_encrypt")).thenReturn(Optional.of(encryptTable));
        when(result.containsConfigDataType("t_encrypt", "certificate_number")).thenReturn(true);
        when(result.containsConfigDataType("t_encrypt", "certificate_number_new")).thenReturn(true);
        EncryptColumn column = mockEncryptColumn();
        when(result.findEncryptColumn("t_encrypt", "certificate_number")).thenReturn(Optional.of(column));
        EncryptColumn newColumn = mockNewEncryptColumn();
        when(result.findEncryptColumn("t_encrypt", "certificate_number_new")).thenReturn(Optional.of(newColumn));
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        Map<String, Integer> dataTypes = new HashMap<>();
        dataTypes.put("int", Types.INTEGER);
        dataTypes.put("varchar", Types.VARCHAR);
        EncryptColumnDataType logicDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        EncryptColumnDataType cipherDataType = new EncryptColumnDataType("varchar(200) not null default ''", dataTypes);
        EncryptColumnDataType assistedQueryDataType = new EncryptColumnDataType("varchar(200) not null", dataTypes);
        EncryptColumnDataType plainDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        return new EncryptColumn(logicDataType, "cipher_certificate_number", cipherDataType, 
                "assisted_certificate_number", assistedQueryDataType, "certificate_number_plain", plainDataType, "test");
    }
    
    private EncryptColumn mockNewEncryptColumn() {
        Map<String, Integer> dataTypes = new HashMap<>();
        dataTypes.put("int", Types.INTEGER);
        dataTypes.put("varchar", Types.VARCHAR);
        EncryptColumnDataType logicDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        EncryptColumnDataType cipherDataType = new EncryptColumnDataType("varchar(500) not null", dataTypes);
        EncryptColumnDataType assistedQueryDataType = new EncryptColumnDataType("varchar(200) not null", dataTypes);
        EncryptColumnDataType plainDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        return new EncryptColumn(logicDataType, "cipher_certificate_number_new", cipherDataType,
                "assisted_certificate_number_new", assistedQueryDataType, "certificate_number_new_plain", plainDataType, "test");
    }
}
