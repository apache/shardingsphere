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

import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptCreateTableTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;

import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
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

public final class EncryptCreateTableTokenGeneratorTest {
    
    private EncryptCreateTableTokenGenerator generator;
    
    @Before
    public void setup() {
        generator = new EncryptCreateTableTokenGenerator();
        generator.setEncryptRule(buildEncryptRule());
    }
    
    @Test
    public void assertGenerateSQLTokens() {
        Collection<SQLToken> sqlTokens = generator.generateSQLTokens(buildCreateTableStatementContext());
        assertThat(sqlTokens.size(), is(4));
        Iterator<SQLToken> iterator = sqlTokens.iterator();
        assertThat(iterator.next(), instanceOf(RemoveToken.class));
        SubstitutableColumnNameToken cipherToken = (SubstitutableColumnNameToken) iterator.next();
        assertThat(cipherToken.toString(mock(RouteUnit.class)), is("cipher_certificate_number"));
        assertThat(cipherToken.getStartIndex(), is(79));
        assertThat(cipherToken.getStopIndex(), is(42));
        SubstitutableColumnNameToken assistedToken = (SubstitutableColumnNameToken) iterator.next();
        assertThat(assistedToken.toString(mock(RouteUnit.class)), is(", assisted_certificate_number"));
        assertThat(assistedToken.getStartIndex(), is(79));
        assertThat(assistedToken.getStopIndex(), is(42));
        SubstitutableColumnNameToken plainToken = (SubstitutableColumnNameToken) iterator.next();
        assertThat(plainToken.toString(mock(RouteUnit.class)), is(", certificate_number_plain"));
        assertThat(plainToken.getStartIndex(), is(79));
        assertThat(plainToken.getStopIndex(), is(42));
    }
    
    private CreateTableStatementContext buildCreateTableStatementContext() {
        CreateTableStatementContext result = mock(CreateTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getTable().getTableName().getIdentifier().getValue()).thenReturn("t_encrypt");
        ColumnDefinitionSegment segment = new ColumnDefinitionSegment(25, 78,
                new ColumnSegment(25, 42, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false);
        when(result.getSqlStatement().getColumnDefinitions()).thenReturn(Collections.singletonList(segment));
        return result;
    }
    
    private EncryptRule buildEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.getLogicColumns()).thenReturn(Collections.singletonList("t_encrypt"));
        when(result.findEncryptor("t_encrypt", "certificate_number")).thenReturn(Optional.of(mock(EncryptAlgorithm.class)));
        when(result.findEncryptTable("t_encrypt")).thenReturn(Optional.of(encryptTable));
        EncryptColumn column = mockEncryptColumn();
        when(result.getCipherColumn("t_encrypt", "certificate_number")).thenReturn(column.getCipherColumn());
        when(result.findPlainColumn("t_encrypt", "certificate_number")).thenReturn(column.getPlainColumn());
        when(result.findAssistedQueryColumn("t_encrypt", "certificate_number")).thenReturn(column.getAssistedQueryColumn());
        when(encryptTable.findEncryptColumn("certificate_number")).thenReturn(Optional.of(column));
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        return new EncryptColumn("cipher_certificate_number", "assisted_certificate_number", "fuzzy_certificate_number", "certificate_number_plain", "test", null);
    }
}
