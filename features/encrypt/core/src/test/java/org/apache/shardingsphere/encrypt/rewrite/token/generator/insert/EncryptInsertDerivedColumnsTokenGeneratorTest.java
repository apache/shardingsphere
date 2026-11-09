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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Attachable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.pojo.GeneratedKeyInsertColumnToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EncryptInsertDerivedColumnsTokenGeneratorTest {
    
    private final EncryptRule rule = mock(EncryptRule.class);
    
    private final EncryptInsertDerivedColumnsTokenGenerator generator = new EncryptInsertDerivedColumnsTokenGenerator(rule);
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotInsertStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutInsertColumns() {
        assertFalse(generator.isGenerateSQLToken(mock(InsertStatementContext.class)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithInsertColumns() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.containsInsertColumns()).thenReturn(true);
        assertTrue(generator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokensWithoutEncryptTable() {
        assertTrue(generator.generateSQLTokens(mockInsertStatementContextWithTable()).isEmpty());
    }
    
    private InsertStatementContext mockInsertStatementContextWithTable() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        SimpleTableSegment table = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(table.getTableName().getIdentifier().getValue()).thenReturn("foo_tbl");
        when(result.getSqlStatement().getTable()).thenReturn(Optional.of(table));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokensWithoutColumns() {
        mockEncryptTable();
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.emptyList(), 31);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))) {
            assertTrue(generator.generateSQLTokens(insertStatementContext).isEmpty());
        }
    }
    
    private EncryptTable mockEncryptTable() {
        EncryptTable result = mock(EncryptTable.class, RETURNS_DEEP_STUBS);
        when(rule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(result));
        return result;
    }
    
    private InsertStatementContext mockInsertStatementContext(final Collection<ColumnSegment> columns, final int insertColumnsStopIndex) {
        InsertStatementContext result = mockInsertStatementContextWithTable();
        when(result.getSqlStatement().getColumns()).thenReturn(columns);
        if (insertColumnsStopIndex >= 0) {
            when(result.getSqlStatement().getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(20, insertColumnsStopIndex, columns)));
        }
        when(result.getSqlStatement().getDatabaseType()).thenReturn(mock(DatabaseType.class));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokensWithUnencryptedColumn() {
        mockEncryptTable();
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))), 31);
        SQLToken previousToken = mock(SQLToken.class);
        List<SQLToken> previousTokens = Collections.singletonList(previousToken);
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))) {
            assertTrue(generator.generateSQLTokens(insertStatementContext).isEmpty());
            assertThat(previousTokens, contains(sameInstance(previousToken)));
        }
    }
    
    @Test
    void assertGenerateSQLTokensWithoutDerivedColumns() {
        mockEncryptTableWithDerivedColumns(false, false);
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))), 31);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext("foo_id", true, true)));
        SQLToken generatedKeyToken = new GeneratedKeyInsertColumnToken(31, "foo_id");
        List<SQLToken> previousTokens = Collections.singletonList(generatedKeyToken);
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))) {
            assertTrue(generator.generateSQLTokens(insertStatementContext).isEmpty());
            assertThat(previousTokens, contains(sameInstance(generatedKeyToken)));
        }
    }
    
    private EncryptTable mockEncryptTableWithDerivedColumns(final boolean assistedQuery, final boolean likeQuery) {
        EncryptTable result = mockEncryptTable();
        when(result.isEncryptColumn("foo_col")).thenReturn(true);
        if (assistedQuery) {
            AssistedQueryColumnItem assistedColumn = mock(AssistedQueryColumnItem.class);
            when(assistedColumn.getName()).thenReturn("foo_assisted");
            when(result.getEncryptColumn("foo_col").getAssistedQuery()).thenReturn(Optional.of(assistedColumn));
        }
        if (likeQuery) {
            LikeQueryColumnItem likeColumn = mock(LikeQueryColumnItem.class);
            when(likeColumn.getName()).thenReturn("foo_like");
            when(result.getEncryptColumn("foo_col").getLikeQuery()).thenReturn(Optional.of(likeColumn));
        }
        return result;
    }
    
    private GeneratedKeyContext createGeneratedKeyContext(final String columnName, final boolean generated, final boolean hasValues) {
        GeneratedKeyContext result = new GeneratedKeyContext(columnName, generated);
        if (hasValues) {
            result.getGeneratedValues().add(1);
        }
        return result;
    }
    
    @ParameterizedTest(name = "assisted={0}, like={1}")
    @MethodSource("getDerivedColumnCases")
    void assertGenerateSQLTokensWithDerivedColumns(final boolean assistedQuery, final boolean likeQuery, final String expectedColumns) {
        mockEncryptTableWithDerivedColumns(assistedQuery, likeQuery);
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))), 31);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext("foo_id", true, true)));
        SQLToken prefixToken = mock(SQLToken.class);
        SQLToken generatedKeyToken = new GeneratedKeyInsertColumnToken(31, "foo_id");
        SQLToken cipherToken = mock(SQLToken.class);
        List<SQLToken> previousTokens = new LinkedList<>(Arrays.asList(prefixToken, generatedKeyToken, cipherToken));
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(QuoteCharacter.NONE)) {
            Collection<SQLToken> actual = generator.generateSQLTokens(insertStatementContext);
            assertThat(actual.size(), is(1));
            SQLToken actualToken = actual.iterator().next();
            assertThat(actualToken, isA(Attachable.class));
            assertThat(actualToken.getStartIndex(), is(31));
            assertThat(actualToken.getStopIndex(), is(31));
            assertThat(actualToken.toString(), is(expectedColumns));
            assertThat(previousTokens, contains(sameInstance(prefixToken), sameInstance(cipherToken)));
        }
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockDatabaseTypeRegistry(final QuoteCharacter quoteCharacter) {
        return mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                (registry, context) -> when(registry.getDialectDatabaseMetaData().getQuoteCharacter()).thenReturn(quoteCharacter));
    }
    
    private static Stream<Arguments> getDerivedColumnCases() {
        return Stream.of(Arguments.of(true, false, ", foo_assisted, foo_id"), Arguments.of(false, true, ", foo_like, foo_id"), Arguments.of(true, true, ", foo_assisted, foo_like, foo_id"));
    }
    
    @Test
    void assertGenerateSQLTokensWithQuotedColumns() {
        mockEncryptTableWithDerivedColumns(true, true);
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))), 31);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext("\"Foo_Id\"", true, true)));
        List<SQLToken> previousTokens = new LinkedList<>(Collections.singleton(new GeneratedKeyInsertColumnToken(31, "\"Foo_Id\"")));
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(QuoteCharacter.BACK_QUOTE)) {
            Collection<SQLToken> actual = generator.generateSQLTokens(insertStatementContext);
            assertThat(actual.size(), is(1));
            SQLToken actualToken = actual.iterator().next();
            assertThat(actualToken, isA(Attachable.class));
            assertThat(actualToken.getStartIndex(), is(31));
            assertThat(actualToken.getStopIndex(), is(31));
            assertThat(actualToken.toString(), is(", `foo_assisted`, `foo_like`, \"Foo_Id\""));
            assertTrue(previousTokens.isEmpty());
        }
    }
    
    @ParameterizedTest(name = "context={0}, generated={1}, values={2}")
    @MethodSource("getGeneratedKeyCases")
    void assertGenerateSQLTokensWithoutGeneratedKey(final boolean hasKeyContext, final boolean generated, final boolean hasValues) {
        mockEncryptTableWithDerivedColumns(true, false);
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))), 31);
        if (hasKeyContext) {
            when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext("foo_id", generated, hasValues)));
        }
        SQLToken prefixToken = mock(SQLToken.class);
        List<SQLToken> previousTokens = Collections.singletonList(prefixToken);
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(QuoteCharacter.NONE)) {
            Collection<SQLToken> actual = generator.generateSQLTokens(insertStatementContext);
            assertThat(actual.size(), is(1));
            SQLToken actualToken = actual.iterator().next();
            assertThat(actualToken, isA(Attachable.class));
            assertThat(actualToken.getStartIndex(), is(31));
            assertThat(actualToken.getStopIndex(), is(31));
            assertThat(actualToken.toString(), is(", foo_assisted"));
            assertThat(previousTokens, contains(sameInstance(prefixToken)));
        }
    }
    
    private static Stream<Arguments> getGeneratedKeyCases() {
        return Stream.of(Arguments.of(false, false, false), Arguments.of(true, false, true), Arguments.of(true, true, false));
    }
    
    @Test
    void assertGenerateSQLTokensWithMultipleColumns() {
        EncryptTable encryptTable = mockEncryptTableWithDerivedColumns(true, false);
        when(encryptTable.isEncryptColumn("bar_col")).thenReturn(true);
        LikeQueryColumnItem likeColumn = mock(LikeQueryColumnItem.class);
        when(likeColumn.getName()).thenReturn("bar_like");
        when(encryptTable.getEncryptColumn("bar_col").getLikeQuery()).thenReturn(Optional.of(likeColumn));
        Collection<ColumnSegment> columns = Arrays.asList(new ColumnSegment(20, 26, new IdentifierValue("foo_col")), new ColumnSegment(29, 34, new IdentifierValue("status")),
                new ColumnSegment(37, 43, new IdentifierValue("bar_col")));
        InsertStatementContext insertStatementContext = mockInsertStatementContext(columns, 44);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext("foo_id", true, true)));
        SQLToken prefixToken = mock(SQLToken.class);
        SQLToken generatedKeyToken = new GeneratedKeyInsertColumnToken(44, "foo_id");
        SQLToken cipherToken = mock(SQLToken.class);
        List<SQLToken> previousTokens = new LinkedList<>(Arrays.asList(prefixToken, generatedKeyToken, cipherToken));
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(QuoteCharacter.NONE)) {
            Collection<SQLToken> actual = generator.generateSQLTokens(insertStatementContext);
            assertThat(actual.size(), is(2));
            Iterator<SQLToken> actualTokens = actual.iterator();
            SQLToken actualFirstToken = actualTokens.next();
            assertThat(actualFirstToken, isA(Attachable.class));
            assertThat(actualFirstToken.getStartIndex(), is(27));
            assertThat(actualFirstToken.getStopIndex(), is(27));
            assertThat(actualFirstToken.toString(), is(", foo_assisted"));
            SQLToken actualLastToken = actualTokens.next();
            assertThat(actualLastToken, isA(Attachable.class));
            assertThat(actualLastToken.getStartIndex(), is(44));
            assertThat(actualLastToken.getStopIndex(), is(44));
            assertThat(actualLastToken.toString(), is(", bar_like, foo_id"));
            assertThat(previousTokens, contains(sameInstance(prefixToken), sameInstance(cipherToken)));
        }
    }
    
    @Test
    void assertGenerateSQLTokensWithGapBeforeClosingParenthesis() {
        mockEncryptTableWithDerivedColumns(true, false);
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.singleton(new ColumnSegment(24, 30, new IdentifierValue("foo_col"))), 33);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(createGeneratedKeyContext("foo_id", true, true)));
        SQLToken generatedKeyToken = new GeneratedKeyInsertColumnToken(33, "foo_id");
        List<SQLToken> previousTokens = Collections.singletonList(generatedKeyToken);
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(QuoteCharacter.NONE)) {
            Collection<SQLToken> actual = generator.generateSQLTokens(insertStatementContext);
            assertThat(actual.size(), is(1));
            SQLToken actualToken = actual.iterator().next();
            assertThat(actualToken, isA(Attachable.class));
            assertThat(actualToken.getStartIndex(), is(31));
            assertThat(actualToken.getStopIndex(), is(31));
            assertThat(actualToken.toString(), is(", foo_assisted"));
            assertThat(previousTokens, contains(sameInstance(generatedKeyToken)));
        }
    }
    
    @Test
    void assertGenerateSQLTokensWithoutInsertColumnsSegment() {
        mockEncryptTable();
        InsertStatementContext insertStatementContext = mockInsertStatementContext(Collections.emptyList(), -1);
        SQLToken prefixToken = mock(SQLToken.class);
        List<SQLToken> previousTokens = Collections.singletonList(prefixToken);
        generator.setPreviousSQLTokens(previousTokens);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))) {
            assertTrue(generator.generateSQLTokens(insertStatementContext).isEmpty());
            assertThat(previousTokens, contains(sameInstance(prefixToken)));
        }
    }
}
