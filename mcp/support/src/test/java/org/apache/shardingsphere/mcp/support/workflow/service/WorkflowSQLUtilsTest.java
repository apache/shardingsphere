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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class WorkflowSQLUtilsTest {
    
    @Test
    void assertCheckSafeIdentifierAllowsSafeIdentifier() {
        assertDoesNotThrow(() -> WorkflowSQLUtils.checkSupportedIdentifier("table", "orders_01"));
    }
    
    @Test
    void assertCheckSupportedIdentifierAllowsSpecialCharacterIdentifier() {
        assertDoesNotThrow(() -> WorkflowSQLUtils.checkSupportedIdentifier("table", "bad table"));
    }
    
    @Test
    void assertCheckSupportedIdentifierRejectsLineTerminator() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSQLUtils.checkSupportedIdentifier("table", "bad\ntable"));
        assertThat(actualException.getMessage(), is("table `bad\ntable` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertCheckSupportedIdentifierRejectsBackQuote() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSQLUtils.checkSupportedIdentifier("table", "bad`table"));
        assertThat(actualException.getMessage(), is("table `bad`table` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertNormalizeIdentifierUnwrapsDelimitedIdentifier() {
        assertThat(WorkflowSQLUtils.normalizeIdentifier("`bad table`"), is("bad table"));
        assertThat(WorkflowSQLUtils.normalizeIdentifier("\"Order Detail\""), is("Order Detail"));
        assertThat(WorkflowSQLUtils.normalizeIdentifier("[Order Detail]"), is("Order Detail"));
    }
    
    @Test
    void assertCanonicalizeIdentifierFoldsPostgreSQLUnquotedIdentifier() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockIdentifierPatternType("PostgreSQL", IdentifierPatternType.LOWER_CASE, typedSPILoader, databaseTypedSPILoader);
            mockIdentifierPatternType("openGauss", IdentifierPatternType.LOWER_CASE, typedSPILoader, databaseTypedSPILoader);
            assertThat(WorkflowSQLUtils.canonicalizeIdentifier("PostgreSQL", "Phone"), is("phone"));
            assertThat(WorkflowSQLUtils.canonicalizeIdentifier("openGauss", "Phone"), is("phone"));
        }
    }
    
    @Test
    void assertCanonicalizeIdentifierKeepsMySQLUnquotedIdentifier() {
        assertThat(WorkflowSQLUtils.canonicalizeIdentifier("MySQL", "Phone"), is("Phone"));
    }
    
    @Test
    void assertCanonicalizeIdentifierPreservesSpecialIdentifier() {
        assertThat(WorkflowSQLUtils.canonicalizeIdentifier("PostgreSQL", "Phone Number"), is("Phone Number"));
    }
    
    @Test
    void assertCanonicalizeIdentifierPreservesDelimitedIdentifier() {
        assertThat(WorkflowSQLUtils.canonicalizeIdentifier("PostgreSQL", "\"Phone\""), is("Phone"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierKeepsSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("orders_01");
        assertThat(actualValue, is("orders_01"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesDelimitedSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("`orders`");
        assertThat(actualValue, is("`orders`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesReservedIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("key");
        assertThat(actualValue, is("`key`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierKeepsPlainIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("rank");
        assertThat(actualValue, is("rank"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistSQLKeywordCases")
    void assertFormatDistSQLIdentifierQuotesDistSQLKeyword(final String name, final String identifier, final String expectedValue) {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier(identifier);
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertFormatDistSQLIdentifierKeepsMixedCaseIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("Phone");
        assertThat(actualValue, is("Phone"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierQuotesUnicodeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatDistSQLIdentifier("订单");
        assertThat(actualValue, is("`订单`"));
    }
    
    @Test
    void assertFormatDistSQLIdentifierRejectsBackQuote() {
        Exception actualException = assertThrows(RuntimeException.class, () -> WorkflowSQLUtils.formatDistSQLIdentifier("bad`table"));
        assertThat(actualException.getMessage(), is("identifier `bad`table` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertFormatGeneratedRuleDistSQLIdentifierQuotesSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier("orders_01");
        assertThat(actualValue, is("`orders_01`"));
    }
    
    @Test
    void assertFormatGeneratedRuleDistSQLIdentifierQuotesDelimitedSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier("`orders`");
        assertThat(actualValue, is("`orders`"));
    }
    
    @Test
    void assertFormatGeneratedRuleDistSQLIdentifierReturnsEmptyForBlankIdentifier() {
        String actualValue = WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier("");
        assertThat(actualValue, is(""));
    }
    
    @Test
    void assertFormatSQLIdentifierUsesMysqlQuoteStyle() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockQuoteCharacter("MySQL", QuoteCharacter.BACK_QUOTE, typedSPILoader, databaseTypedSPILoader);
            String actualValue = WorkflowSQLUtils.formatSQLIdentifier("MySQL", "order detail");
            assertThat(actualValue, is("`order detail`"));
        }
    }
    
    @Test
    void assertFormatSQLIdentifierKeepsSafeIdentifier() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("MySQL", "orders_01");
        assertThat(actualValue, is("orders_01"));
    }
    
    @Test
    void assertFormatSQLIdentifierKeepsPostgreSQLMixedCaseIdentifier() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("PostgreSQL", "Phone");
        assertThat(actualValue, is("Phone"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSQLPlainIdentifierCases")
    void assertFormatSQLIdentifierKeepsPlainIdentifier(final String name, final String databaseType, final String identifier, final String expectedValue) {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier(databaseType, identifier);
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertFormatSQLIdentifierUsesFallbackQuoteStyle() {
        String actualValue = WorkflowSQLUtils.formatSQLIdentifier("", "order detail");
        assertThat(actualValue, is("`order detail`"));
    }
    
    @Test
    void assertFormatSQLIdentifierUsesPostgreSQLQuoteStyle() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockQuoteCharacter("PostgreSQL", QuoteCharacter.QUOTE, typedSPILoader, databaseTypedSPILoader);
            String actualValue = WorkflowSQLUtils.formatSQLIdentifier("PostgreSQL", "order detail");
            assertThat(actualValue, is("\"order detail\""));
        }
    }
    
    @Test
    void assertFormatSQLIdentifierUsesSQLServerQuoteStyle() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockQuoteCharacter("SQLServer", QuoteCharacter.BRACKETS, typedSPILoader, databaseTypedSPILoader);
            String actualValue = WorkflowSQLUtils.formatSQLIdentifier("SQLServer", "order detail");
            assertThat(actualValue, is("[order detail]"));
        }
    }
    
    @Test
    void assertFormatSQLIdentifierPreservesDelimitedSafeIdentifier() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockQuoteCharacter("PostgreSQL", QuoteCharacter.QUOTE, typedSPILoader, databaseTypedSPILoader);
            String actualValue = WorkflowSQLUtils.formatSQLIdentifier("PostgreSQL", "\"orders\"");
            assertThat(actualValue, is("\"orders\""));
        }
    }
    
    @Test
    void assertIsSameIdentifierWithCaseInsensitiveDatabase() {
        assertTrue(WorkflowSQLUtils.isSameIdentifier("MySQL", "Phone", "phone"));
    }
    
    @Test
    void assertIsSameIdentifierFoldsPostgreSQLUnquotedIdentifier() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockIdentifierPatternType("PostgreSQL", IdentifierPatternType.LOWER_CASE, typedSPILoader, databaseTypedSPILoader);
            assertTrue(WorkflowSQLUtils.isSameIdentifier("PostgreSQL", "Phone", "phone"));
        }
    }
    
    @Test
    void assertIsSameIdentifierPreservesPostgreSQLDelimitedIdentifier() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockIdentifierPatternType("PostgreSQL", IdentifierPatternType.LOWER_CASE, typedSPILoader, databaseTypedSPILoader);
            assertFalse(WorkflowSQLUtils.isSameIdentifier("PostgreSQL", "\"Phone\"", "phone"));
        }
    }
    
    @Test
    void assertIsSameIdentifierRejectsUnquotedPostgreSQLQuotedName() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockIdentifierPatternType("PostgreSQL", IdentifierPatternType.LOWER_CASE, typedSPILoader, databaseTypedSPILoader);
            assertFalse(WorkflowSQLUtils.isSameIdentifier("PostgreSQL", "Phone", "Phone"));
        }
    }
    
    @Test
    void assertIsSameIdentifierMatchesQuotedPostgreSQLName() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockIdentifierPatternType("PostgreSQL", IdentifierPatternType.LOWER_CASE, typedSPILoader, databaseTypedSPILoader);
            assertTrue(WorkflowSQLUtils.isSameIdentifier("PostgreSQL", "\"Phone\"", "Phone"));
        }
    }
    
    @Test
    void assertEscapeLiteralEscapesSingleQuote() {
        String actualValue = WorkflowSQLUtils.escapeLiteral("O'Brien");
        assertThat(actualValue, is("O''Brien"));
    }
    
    @Test
    void assertCreatePropertiesTrimsValues() {
        Properties actualProperties = WorkflowSQLUtils.createProperties(Map.of("aes-key-value", " 123456 "));
        assertThat(actualProperties.getProperty("aes-key-value"), is("123456"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentFormatsProperties() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragment(" AES ", Map.of("aes-key-value", " 123456 "));
        assertThat(actualFragment, is("TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456'))"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentEscapesLiterals() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragment("AES'X", Map.of("a'b", "v'1"));
        assertThat(actualFragment, is("TYPE(NAME='aes''x', PROPERTIES('a''b'='v''1'))"));
    }
    
    @Test
    void assertCreateAlgorithmFragmentReturnsEmptyForBlankType() {
        String actualFragment = WorkflowSQLUtils.createAlgorithmFragment(" ", Map.of("aes-key-value", "123456"));
        assertThat(actualFragment, is(""));
    }
    
    @Test
    void assertCreatePropertyMapReturnsEmptyForNull() {
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap(null);
        assertThat(actualEntries, is(Map.of()));
    }
    
    @Test
    void assertCreatePropertyMapHandlesProperties() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", " 123456 ");
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap(props);
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesMap() {
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap(Map.of("aes-key-value", " 123456 "));
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456")));
    }
    
    @Test
    void assertCreatePropertyMapHandlesString() {
        Map<String, String> actualEntries = WorkflowSQLUtils.createPropertyMap("{'aes-key-value':'123456','iv':'abc'}");
        assertThat(actualEntries, is(Map.of("aes-key-value", "123456", "iv", "abc")));
    }
    
    private static Stream<Arguments> getDistSQLKeywordCases() {
        return Stream.of(
                Arguments.of("quote if keyword", "if", "`if`"),
                Arguments.of("quote exists keyword", "exists", "`exists`"),
                Arguments.of("quote true keyword", "true", "`true`"),
                Arguments.of("quote false keyword", "false", "`false`"),
                Arguments.of("quote name keyword", "name", "`name`"),
                Arguments.of("quote cipher keyword", "cipher", "`cipher`"),
                Arguments.of("quote order keyword", "order", "`order`"),
                Arguments.of("quote type keyword", "type", "`type`"),
                Arguments.of("quote table keyword", "table", "`table`"),
                Arguments.of("quote from keyword", "from", "`from`"),
                Arguments.of("quote properties keyword", "properties", "`properties`"),
                Arguments.of("quote encrypt algorithm keyword", "encrypt_algorithm", "`encrypt_algorithm`"),
                Arguments.of("quote assisted query column keyword", "assisted_query_column", "`assisted_query_column`"),
                Arguments.of("quote mask algorithm keyword", "keep_from_x_to_y", "`keep_from_x_to_y`"),
                Arguments.of("quote uppercase algorithm keyword", "AES", "`AES`"));
    }
    
    private static Stream<Arguments> getSQLPlainIdentifierCases() {
        return Stream.of(
                Arguments.of("keep MySQL rank identifier", "MySQL", "rank", "rank"),
                Arguments.of("keep MySQL user identifier", "MySQL", "user", "user"),
                Arguments.of("keep PostgreSQL key identifier", "PostgreSQL", "key", "key"),
                Arguments.of("keep PostgreSQL user identifier", "PostgreSQL", "user", "user"));
    }
    
    private static void mockQuoteCharacter(final String databaseType, final QuoteCharacter quoteCharacter,
                                           final MockedStatic<TypedSPILoader> typedSPILoader, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, typedSPILoader, databaseTypedSPILoader);
        when(dialectDatabaseMetaData.getQuoteCharacter()).thenReturn(quoteCharacter);
    }
    
    private static void mockIdentifierPatternType(final String databaseType, final IdentifierPatternType identifierPatternType,
                                                  final MockedStatic<TypedSPILoader> typedSPILoader, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, typedSPILoader, databaseTypedSPILoader);
        when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(identifierPatternType);
    }
    
    private static DialectDatabaseMetaData mockDialectDatabaseMetaData(final String databaseType,
                                                                       final MockedStatic<TypedSPILoader> typedSPILoader,
                                                                       final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(Optional.of(result));
        return result;
    }
}
