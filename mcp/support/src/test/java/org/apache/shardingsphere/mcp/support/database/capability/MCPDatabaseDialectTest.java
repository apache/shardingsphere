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
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPDatabaseDialectTest {
    
    @Test
    void assertOf() {
        MCPDatabaseDialect actual = MCPDatabaseDialect.of("");
        assertThat(actual.getIdentifierQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
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
    void assertGetIdentifierQuoteCharacterWithUnknownDatabaseType() {
        QuoteCharacter actual = MCPDatabaseDialect.of("FixtureDB").getIdentifierQuoteCharacter();
        assertThat(actual, is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetIdentifierCasePolicyWithCaseInsensitiveIdentifier() {
        IdentifierCasePolicy actual = MCPDatabaseDialect.of("MySQL").getIdentifierCasePolicy(IdentifierScope.TABLE);
        assertTrue(actual.matches("phone", "Phone", QuoteCharacter.QUOTE));
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDefaultSchemaSemanticsArguments")
    void assertGetDefaultSchemaSemantics(final String name, final String databaseType, final SchemaSemantics expected) {
        SchemaSemantics actual = MCPDatabaseDialect.of(databaseType).getDefaultSchemaSemantics();
        assertThat(actual, is(expected));
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
    void assertIsUnquotedIdentifierCaseFoldedWithUnknownDatabaseType() {
        boolean actual = MCPDatabaseDialect.of("FixtureDB").isUnquotedIdentifierCaseFolded();
        assertFalse(actual);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSequenceQueryArguments")
    void assertGetSequenceQuery(final String name, final String databaseType, final String expected) {
        String actual = MCPDatabaseDialect.of(databaseType).getSequenceQuery().orElse("");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertIsSystemSchema() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("Fixture", typedSPILoader);
            mockDialectDatabaseMetaDataAbsent(databaseType, databaseTypedSPILoader);
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
            mockDialectDatabaseMetaDataAbsent(databaseType, databaseTypedSPILoader);
            DialectSystemDatabase dialectSystemDatabase = mockDialectSystemDatabase(databaseType, databaseTypedSPILoader);
            when(dialectSystemDatabase.getSystemSchemas()).thenReturn(List.of("fixture_system"));
            boolean actual = MCPDatabaseDialect.of("Fixture").isSystemSchema("", "fixture_system", SchemaSemantics.DATABASE_AS_SCHEMA);
            assertTrue(actual);
        }
    }
    
    @Test
    void assertIsSystemSchemaWithOptionSystemSchema() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("FixtureWithOption", typedSPILoader);
            mockDialectDatabaseMetaDataAbsent(databaseType, databaseTypedSPILoader);
            DialectSystemDatabase dialectSystemDatabase = mockDialectSystemDatabase(databaseType, databaseTypedSPILoader);
            when(dialectSystemDatabase.getSystemSchemas()).thenReturn(List.of("dialect_system"));
            MCPDatabaseCapabilityOption option = mockMCPDatabaseCapabilityOption("FixtureWithOption", typedSPILoader);
            when(option.getSystemSchemas()).thenReturn(List.of("option_system"));
            boolean actual = MCPDatabaseDialect.of("FixtureWithOption").isSystemSchema("option_system");
            assertTrue(actual);
        }
    }
    
    @Test
    void assertIsSystemSchemaIgnoresDifferentDialectSystemSchema() {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mockDatabaseType("FixtureWithOption", typedSPILoader);
            mockDialectDatabaseMetaDataAbsent(databaseType, databaseTypedSPILoader);
            DialectSystemDatabase dialectSystemDatabase = mockDialectSystemDatabase(databaseType, databaseTypedSPILoader);
            when(dialectSystemDatabase.getSystemSchemas()).thenReturn(List.of("dialect_system"));
            MCPDatabaseCapabilityOption option = mockMCPDatabaseCapabilityOption("FixtureWithOption", typedSPILoader);
            when(option.getSystemSchemas()).thenReturn(List.of("option_system"));
            boolean actual = MCPDatabaseDialect.of("FixtureWithOption").isSystemSchema("dialect_system");
            assertFalse(actual);
        }
    }
    
    @Test
    void assertIsSystemSchemaWithUserSchema() {
        boolean actual = MCPDatabaseDialect.of("MySQL").isSystemSchema("orders");
        assertFalse(actual);
    }
    
    @Test
    void assertIsSystemSchemaIgnoresUnknownDatabaseType() {
        boolean actual = MCPDatabaseDialect.of("FixtureDB").isSystemSchema("INFORMATION_SCHEMA");
        assertFalse(actual);
    }
    
    @Test
    void assertIsSystemSchemaIgnoresOtherDatabaseType() {
        boolean actual = MCPDatabaseDialect.of("SQLServer").isSystemSchema("pg_catalog");
        assertFalse(actual);
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
    
    private static void mockDialectDatabaseMetaDataAbsent(final DatabaseType databaseType, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)).thenReturn(Optional.empty());
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
    
    private static Stream<Arguments> getDefaultSchemaSemanticsArguments() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", SchemaSemantics.DATABASE_AS_SCHEMA),
                Arguments.of("postgresql", "PostgreSQL", SchemaSemantics.NATIVE_SCHEMA),
                Arguments.of("unknown", "FixtureDB", SchemaSemantics.NATIVE_SCHEMA));
    }
    
    private static Stream<Arguments> getSequenceQueryArguments() {
        return Stream.of(
                Arguments.of("postgresql", "PostgreSQL", "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"),
                Arguments.of("sql server", "SQLServer",
                        "SELECT schemas.name AS SEQUENCE_SCHEMA, seq.name AS SEQUENCE_NAME FROM sys.sequences seq INNER JOIN sys.schemas schemas ON seq.schema_id = schemas.schema_id"),
                Arguments.of("mysql", "MySQL", ""));
    }
    
}
