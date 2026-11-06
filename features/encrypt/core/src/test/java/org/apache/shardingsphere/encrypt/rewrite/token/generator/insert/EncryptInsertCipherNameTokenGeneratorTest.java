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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.insert;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture.EncryptGeneratorFixtureBuilder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class EncryptInsertCipherNameTokenGeneratorTest {
    
    private EncryptInsertCipherNameTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        generator = new EncryptInsertCipherNameTokenGenerator(EncryptGeneratorFixtureBuilder.createEncryptRule());
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotInsertStatement() {
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithoutInsertColumns() {
        assertFalse(generator.isGenerateSQLToken(mock(InsertStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithEmptyInsertColumns() {
        assertFalse(generator.isGenerateSQLToken(mockInsertStatementContext(Collections.emptyList())));
    }
    
    private InsertStatementContext mockInsertStatementContext(final Collection<ColumnSegment> columns) {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getSqlStatement().getTable()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        when(result.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(20, 31, columns)));
        when(result.getInsertSelectContext()).thenReturn(null);
        return result;
    }
    
    @Test
    void assertIsGenerateSQLTokenWithInsertStatementContext() {
        assertTrue(generator.isGenerateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Collections.emptyList())));
    }
    
    @Test
    void assertGenerateSQLTokensWithInsertStatementContext() {
        Collection<SQLToken> actual = generator.generateSQLTokens(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Collections.emptyList()));
        assertThat(actual.size(), is(1));
        assertThat(((SubstitutableColumnNameToken) actual.iterator().next()).toString(null), is("pwd_cipher, pwd_assist, pwd_like"));
    }
    
    @Test
    void assertGenerateSQLTokensWithoutEncryptTable() {
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))));
        assertTrue(new EncryptInsertCipherNameTokenGenerator(mock(EncryptRule.class)).generateSQLTokens(insertStatementContext).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensWithoutEncryptColumn() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(mock(EncryptTable.class)));
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))));
        assertTrue(new EncryptInsertCipherNameTokenGenerator(encryptRule).generateSQLTokens(insertStatementContext).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensWithEmptyInsertColumns() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(mock(EncryptTable.class)));
        assertTrue(new EncryptInsertCipherNameTokenGenerator(encryptRule).generateSQLTokens(mockInsertStatementContext(Collections.emptyList())).isEmpty());
    }
    
    @ParameterizedTest(name = "{0}: cipher={1}, assisted={2}, like={3}")
    @MethodSource("getDerivedColumnCases")
    void assertGenerateSQLTokensWithDerivedColumns(final QuoteCharacter quoteCharacter, final String cipherColumn, final boolean assistedQuery, final boolean likeQuery, final String expectedColumns) {
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(encryptRule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("foo_col")).thenReturn(true);
        when(encryptTable.getEncryptColumn("foo_col")).thenReturn(encryptColumn);
        when(encryptColumn.getCipher().getName()).thenReturn(cipherColumn);
        if (assistedQuery) {
            AssistedQueryColumnItem assistedQueryColumn = mock(AssistedQueryColumnItem.class);
            when(assistedQueryColumn.getName()).thenReturn("foo_assisted");
            when(encryptColumn.getAssistedQuery()).thenReturn(Optional.of(assistedQueryColumn));
        }
        if (likeQuery) {
            LikeQueryColumnItem likeQueryColumn = mock(LikeQueryColumnItem.class);
            when(likeQueryColumn.getName()).thenReturn("foo_like");
            when(encryptColumn.getLikeQuery()).thenReturn(Optional.of(likeQueryColumn));
        }
        DialectDatabaseMetaData metaData = mock(DialectDatabaseMetaData.class);
        when(metaData.getQuoteCharacter()).thenReturn(quoteCharacter);
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))));
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (registry, context) -> when(registry.getDialectDatabaseMetaData()).thenReturn(metaData))) {
            Collection<SQLToken> actual = new EncryptInsertCipherNameTokenGenerator(encryptRule).generateSQLTokens(insertStatementContext);
            assertThat(actual.size(), is(1));
            SubstitutableColumnNameToken actualToken = (SubstitutableColumnNameToken) actual.iterator().next();
            assertThat(actualToken.getStartIndex(), is(24));
            assertThat(actualToken.getStopIndex(), is(30));
            assertThat(actualToken.toString(null), is(expectedColumns));
        }
    }
    
    private static Stream<Arguments> getDerivedColumnCases() {
        return Stream.of(
                Arguments.of(QuoteCharacter.NONE, "foo_cipher", false, false, "foo_cipher"),
                Arguments.of(QuoteCharacter.NONE, "foo_cipher", true, false, "foo_cipher, foo_assisted"),
                Arguments.of(QuoteCharacter.NONE, "foo_cipher", false, true, "foo_cipher, foo_like"),
                Arguments.of(QuoteCharacter.BACK_QUOTE, "foo_cipher", true, true, "`foo_cipher`, `foo_assisted`, `foo_like`"),
                Arguments.of(QuoteCharacter.BACK_QUOTE, "foo_col", true, true, "`foo_col`, `foo_assisted`, `foo_like`"));
    }
    
    @Test
    void assertGenerateSQLTokensWhenInsertColumnsUseDifferentEncryptorWithSelectProjection() {
        assertThrows(UnsupportedSQLOperationException.class, () -> generator.generateSQLTokens(EncryptGeneratorFixtureBuilder.createInsertSelectStatementContext(true)));
    }
}
