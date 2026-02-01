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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.ColumnDefinitionToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstituteColumnDefinitionToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptCreateTableTokenGeneratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private EncryptCreateTableTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        generator = new EncryptCreateTableTokenGenerator(mockEncryptRule());
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
        Collection<SQLToken> actual = generator.generateSQLTokens(new CommonSQLStatementContext(createCreateTableStatement()));
        assertThat(actual.size(), is(1));
        SQLToken token = actual.iterator().next();
        assertThat(token, isA(SubstituteColumnDefinitionToken.class));
        Collection<SQLToken> columnTokens = ((SubstituteColumnDefinitionToken) token).getColumnDefinitionTokens();
        Iterator<SQLToken> actualIterator = columnTokens.iterator();
        ColumnDefinitionToken cipherToken = (ColumnDefinitionToken) actualIterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number VARCHAR(4000)"));
        assertThat(cipherToken.getStartIndex(), is(25));
        ColumnDefinitionToken assistedToken = (ColumnDefinitionToken) actualIterator.next();
        assertThat(assistedToken.toString(), is("assisted_certificate_number VARCHAR(4000)"));
        assertThat(assistedToken.getStartIndex(), is(25));
        ColumnDefinitionToken likeToken = (ColumnDefinitionToken) actualIterator.next();
        assertThat(likeToken.toString(), is("like_certificate_number VARCHAR(4000)"));
        assertThat(likeToken.getStartIndex(), is(25));
        assertThat(token.toString(), is("cipher_certificate_number VARCHAR(4000), assisted_certificate_number VARCHAR(4000), like_certificate_number VARCHAR(4000)"));
        assertThat(token.getStartIndex(), is(25));
        assertThat(token.getStopIndex(), is(78));
        assertTrue(((SubstituteColumnDefinitionToken) token).isLastColumn());
    }
    
    private CreateTableStatement createCreateTableStatement() {
        CreateTableStatement result = new CreateTableStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt"))));
        result.getColumnDefinitions().add(new ColumnDefinitionSegment(25, 78, new ColumnSegment(25, 42, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false, ""));
        result.buildAttributes();
        return result;
    }
}
