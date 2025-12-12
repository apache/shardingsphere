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
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.RemoveToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptAlterTableTokenGeneratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private EncryptAlterTableTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        generator = new EncryptAlterTableTokenGenerator(mockEncryptRule());
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
        EncryptColumn result = new EncryptColumn("certificate_number", new CipherColumnItem("cipher_certificate_number", mock(EncryptAlgorithm.class)));
        result.setAssistedQuery(new AssistedQueryColumnItem("assisted_certificate_number", mock(EncryptAlgorithm.class)));
        result.setLikeQuery(new LikeQueryColumnItem("like_certificate_number", mock(EncryptAlgorithm.class)));
        return result;
    }
    
    private EncryptColumn mockNewEncryptColumn() {
        EncryptColumn result = new EncryptColumn(
                "certificate_number_new", new CipherColumnItem("cipher_certificate_number_new", mock(EncryptAlgorithm.class)));
        result.setAssistedQuery(new AssistedQueryColumnItem("assisted_certificate_number_new", mock(EncryptAlgorithm.class)));
        result.setLikeQuery(new LikeQueryColumnItem("like_certificate_number_new", mock(EncryptAlgorithm.class)));
        return result;
    }
    
    @Test
    void assertAddColumnGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(new CommonSQLStatementContext(createAddColumnStatement()));
        assertThat(actual.size(), is(4));
        Iterator<SQLToken> actualIterator = actual.iterator();
        assertThat(actualIterator.next(), isA(RemoveToken.class));
        EncryptColumnToken cipherToken = (EncryptColumnToken) actualIterator.next();
        assertThat(cipherToken.toString(), is("cipher_certificate_number VARCHAR(4000)"));
        assertThat(cipherToken.getStartIndex(), is(68));
        assertThat(cipherToken.getStopIndex(), is(67));
        EncryptColumnToken assistedToken = (EncryptColumnToken) actualIterator.next();
        assertThat(assistedToken.toString(), is(", ADD COLUMN assisted_certificate_number VARCHAR(4000)"));
        assertThat(assistedToken.getStartIndex(), is(68));
        assertThat(assistedToken.getStopIndex(), is(67));
        EncryptColumnToken likeToken = (EncryptColumnToken) actualIterator.next();
        assertThat(likeToken.toString(), is(", ADD COLUMN like_certificate_number VARCHAR(4000)"));
        assertThat(likeToken.getStartIndex(), is(68));
        assertThat(likeToken.getStopIndex(), is(67));
    }
    
    private SQLStatement createAddColumnStatement() {
        AlterTableStatement result = new AlterTableStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt"))));
        ColumnDefinitionSegment columnDefinitionSegment = new ColumnDefinitionSegment(
                33, 67, new ColumnSegment(33, 50, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false, "");
        result.getAddColumnDefinitions().add(new AddColumnDefinitionSegment(22, 67, Collections.singleton(columnDefinitionSegment)));
        return result;
    }
    
    @Test
    void assertModifyEncryptColumnGenerateSQLTokens() {
        assertThrows(UnsupportedOperationException.class, () -> generator.generateSQLTokens(new CommonSQLStatementContext(createModifyColumnStatement())));
    }
    
    private SQLStatement createModifyColumnStatement() {
        AlterTableStatement result = new AlterTableStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt"))));
        ColumnDefinitionSegment columnDefinitionSegment = new ColumnDefinitionSegment(
                36, 70, new ColumnSegment(36, 53, new IdentifierValue("certificate_number")), new DataTypeSegment(), false, false, "");
        result.getModifyColumnDefinitions().add(new ModifyColumnDefinitionSegment(22, 70, columnDefinitionSegment));
        return result;
    }
    
    @Test
    void assertChangeEncryptColumnGenerateSQLTokens() {
        assertThrows(UnsupportedOperationException.class, () -> generator.generateSQLTokens(new CommonSQLStatementContext(createChangeColumnStatement())));
    }
    
    private SQLStatement createChangeColumnStatement() {
        AlterTableStatement result = new AlterTableStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt"))));
        ColumnDefinitionSegment columnDefinitionSegment = new ColumnDefinitionSegment(
                55, 93, new ColumnSegment(55, 76, new IdentifierValue("certificate_number_new")), new DataTypeSegment(), false, false, "");
        ChangeColumnDefinitionSegment changeColumnDefinitionSegment = new ChangeColumnDefinitionSegment(22, 93, columnDefinitionSegment);
        changeColumnDefinitionSegment.setPreviousColumn(new ColumnSegment(36, 53, new IdentifierValue("certificate_number")));
        result.getChangeColumnDefinitions().add(changeColumnDefinitionSegment);
        return result;
    }
}
