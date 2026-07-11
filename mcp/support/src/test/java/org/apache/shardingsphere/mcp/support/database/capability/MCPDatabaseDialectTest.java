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

package org.apache.shardingsphere.mcp.support.database.capability;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPDatabaseDialectTest {
    
    @Test
    void assertOfWithEmptyDatabaseType() {
        assertThrows(ServiceProviderNotFoundException.class, () -> MCPDatabaseDialect.of(""));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getIdentifierQuoteCharacterArguments")
    void assertGetIdentifierQuoteCharacter(final String name, final String databaseType, final QuoteCharacter expected) {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseTypeFromSPI = mockDatabaseType(databaseType, typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseTypeFromSPI, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getQuoteCharacter()).thenReturn(expected);
            QuoteCharacter actual = MCPDatabaseDialect.of(databaseType).getIdentifierQuoteCharacter();
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertOfWithUnknownDatabaseType() {
        assertThrows(ServiceProviderNotFoundException.class, () -> MCPDatabaseDialect.of("FixtureDB"));
    }
    
    @Test
    void assertGetIdentifierCasePolicyWithCaseInsensitiveIdentifier() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("FixtureWithOption", typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
            MCPDatabaseCapabilityOption option = mockMCPDatabaseCapabilityOption("FixtureWithOption", typedSPILoader);
            when(option.getIdentifierCasePolicySet()).thenReturn(IdentifierCasePolicyFactory.newMySQLInsensitivePolicySet());
            IdentifierCasePolicy actual = MCPDatabaseDialect.of("FixtureWithOption").getIdentifierCasePolicy(IdentifierScope.TABLE);
            assertTrue(actual.matches("phone", "Phone", QuoteCharacter.QUOTE));
        }
    }
    
    @Test
    void assertGetIdentifierCasePolicyFromLowerCaseDialectDatabaseMetaData() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("FixtureWithOption", typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.LOWER_CASE);
            MCPDatabaseCapabilityOption option = mockMCPDatabaseCapabilityOption("FixtureWithOption", typedSPILoader);
            when(option.getIdentifierCasePolicySet()).thenReturn(IdentifierCasePolicyFactory.newSensitivePolicySet());
            IdentifierCasePolicy actual = MCPDatabaseDialect.of("FixtureWithOption").getIdentifierCasePolicy(IdentifierScope.TABLE);
            assertTrue(actual.matches("fixture", "Fixture", QuoteCharacter.NONE));
        }
    }
    
    @Test
    void assertGetIdentifierCasePolicyFromUpperCaseDialectDatabaseMetaData() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("FixtureWithOption", typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.UPPER_CASE);
            MCPDatabaseCapabilityOption option = mockMCPDatabaseCapabilityOption("FixtureWithOption", typedSPILoader);
            when(option.getIdentifierCasePolicySet()).thenReturn(IdentifierCasePolicyFactory.newSensitivePolicySet());
            IdentifierCasePolicy actual = MCPDatabaseDialect.of("FixtureWithOption").getIdentifierCasePolicy(IdentifierScope.TABLE);
            assertTrue(actual.matches("FIXTURE", "fixture", QuoteCharacter.NONE));
        }
    }
    
    @Test
    void assertGetDefaultSchemaSemantics() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null, DialectSchemaSemantics.DATABASE_AS_SCHEMA));
            assertThat(MCPDatabaseDialect.of("Fixture").getDefaultSchemaSemantics(), is(DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        }
    }
    
    @Test
    void assertOfWithoutDialectDatabaseMetaData() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)).thenReturn(Optional.empty());
            assertThrows(ServiceProviderNotFoundException.class, () -> MCPDatabaseDialect.of("Fixture"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTransactionCapabilityArguments")
    void assertGetTransactionCapability(final String name, final boolean transactionSupported, final boolean savepointSupported, final TransactionCapability expected) {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            assertThat(MCPDatabaseDialect.of("Fixture").getTransactionCapability(transactionSupported, savepointSupported), is(expected));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isUnquotedIdentifierCaseFoldedArguments")
    void assertIsUnquotedIdentifierCaseFolded(final String name, final String databaseType, final IdentifierPatternType identifierPatternType, final boolean expected) {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseTypeFromSPI = mockDatabaseType(databaseType, typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseTypeFromSPI, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(identifierPatternType);
            boolean actual = MCPDatabaseDialect.of(databaseType).isUnquotedIdentifierCaseFolded();
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertIsSequenceSupported() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(Optional.of(new DialectSequenceOption("SELECT 1")));
            assertTrue(MCPDatabaseDialect.of("Fixture").isSequenceSupported());
        }
    }
    
    @Test
    void assertIsSequenceSupportedAbsent() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(Optional.empty());
            assertFalse(MCPDatabaseDialect.of("Fixture").isSequenceSupported());
        }
    }
    
    @Test
    void assertIsSystemSchema() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            DialectSystemDatabase dialectSystemDatabase = mockDialectSystemDatabase(databaseType, databaseTypedSPILoader);
            when(dialectSystemDatabase.getSystemSchemas()).thenReturn(List.of("fixture_system"));
            boolean actual = MCPDatabaseDialect.of("Fixture").isSystemSchema("fixture_system");
            assertTrue(actual);
        }
    }
    
    @Test
    void assertIsSystemSchemaWithCatalog() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            DialectSystemDatabase dialectSystemDatabase = mockDialectSystemDatabase(databaseType, databaseTypedSPILoader);
            when(dialectSystemDatabase.getSystemSchemas()).thenReturn(List.of("fixture_system"));
            boolean actual = MCPDatabaseDialect.of("Fixture").isSystemSchema("", "fixture_system", DialectSchemaSemantics.DATABASE_AS_SCHEMA);
            assertTrue(actual);
        }
    }
    
    @Test
    void assertIsSystemSchemaWithoutDialectSystemDatabase() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.empty());
            assertFalse(MCPDatabaseDialect.of("Fixture").isSystemSchema("information_schema"));
        }
    }
    
    @Test
    void assertIsSystemSchemaWithOtherSchema() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            mockDialectDatabaseMetaData(databaseType, databaseTypedSPILoader);
            DialectSystemDatabase dialectSystemDatabase = mockDialectSystemDatabase(databaseType, databaseTypedSPILoader);
            when(dialectSystemDatabase.getSystemSchemas()).thenReturn(List.of("fixture_system"));
            assertFalse(MCPDatabaseDialect.of("Fixture").isSystemSchema("pg_catalog"));
        }
    }
    
    private static Stream<Arguments> getIdentifierQuoteCharacterArguments() {
        return Stream.of(
                Arguments.of("back quote", "FixtureBackQuote", QuoteCharacter.BACK_QUOTE),
                Arguments.of("brackets", "FixtureBrackets", QuoteCharacter.BRACKETS),
                Arguments.of("quote", "FixtureQuote", QuoteCharacter.QUOTE));
    }
    
    private static Stream<Arguments> isUnquotedIdentifierCaseFoldedArguments() {
        return Stream.of(
                Arguments.of("lower case", "FixtureLowerCase", IdentifierPatternType.LOWER_CASE, true),
                Arguments.of("upper case", "FixtureUpperCase", IdentifierPatternType.UPPER_CASE, true),
                Arguments.of("keep origin", "FixtureKeepOrigin", IdentifierPatternType.KEEP_ORIGIN, false));
    }
    
    private static DatabaseType mockDatabaseType(final String databaseType, final MockedStatic<TypedSPILoader> typedSPILoader) {
        DatabaseType result = mock(DatabaseType.class);
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(result));
        typedSPILoader.when(() -> TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType)).thenReturn(Optional.empty());
        return result;
    }
    
    private static DialectDatabaseMetaData mockDialectDatabaseMetaData(final DatabaseType databaseType, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)).thenReturn(Optional.of(result));
        return result;
    }
    
    private static MCPDatabaseCapabilityOption mockMCPDatabaseCapabilityOption(final String databaseType, final MockedStatic<TypedSPILoader> typedSPILoader) {
        MCPDatabaseCapabilityOption result = mock(MCPDatabaseCapabilityOption.class);
        typedSPILoader.when(() -> TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType)).thenReturn(Optional.of(result));
        return result;
    }
    
    private static DialectSystemDatabase mockDialectSystemDatabase(final DatabaseType databaseType, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DialectSystemDatabase result = mock(DialectSystemDatabase.class);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(result));
        return result;
    }
    
    private static Stream<Arguments> getTransactionCapabilityArguments() {
        return Stream.of(
                Arguments.of("not supported", false, false, TransactionCapability.NONE),
                Arguments.of("local only", true, false, TransactionCapability.LOCAL),
                Arguments.of("local with savepoint", true, true, TransactionCapability.LOCAL_WITH_SAVEPOINT));
    }
    
}
