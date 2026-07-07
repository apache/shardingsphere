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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDatabaseDialectTest {
    
    @Test
    void assertOf() {
        MCPDatabaseDialect actual = MCPDatabaseDialect.of("");
        assertThat(actual.getIdentifierQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getIdentifierQuoteCharacterArguments")
    void assertGetIdentifierQuoteCharacter(final String name, final String databaseType, final QuoteCharacter expected) {
        QuoteCharacter actual = MCPDatabaseDialect.of(databaseType).getIdentifierQuoteCharacter();
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetIdentifierCasePolicyWithCaseInsensitiveIdentifier() {
        IdentifierCasePolicy actual = MCPDatabaseDialect.of("MySQL").getIdentifierCasePolicy(IdentifierScope.TABLE);
        assertTrue(actual.matches("phone", "Phone", QuoteCharacter.QUOTE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDefaultSchemaSemanticsArguments")
    void assertGetDefaultSchemaSemantics(final String name, final String databaseType, final SchemaSemantics expected) {
        SchemaSemantics actual = MCPDatabaseDialect.of(databaseType).getDefaultSchemaSemantics();
        assertThat(actual, is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isUnquotedIdentifierCaseFoldedArguments")
    void assertIsUnquotedIdentifierCaseFolded(final String name, final String databaseType, final boolean expected) {
        boolean actual = MCPDatabaseDialect.of(databaseType).isUnquotedIdentifierCaseFolded();
        assertThat(actual, is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSequenceQueryArguments")
    void assertGetSequenceQuery(final String name, final String databaseType, final String expected) {
        String actual = MCPDatabaseDialect.of(databaseType).getSequenceQuery().orElse("");
        assertThat(actual, is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isInformationSchemaColumnSchemaFilterRequiredArguments")
    void assertIsInformationSchemaColumnSchemaFilterRequired(final String name, final String databaseType, final boolean expected) {
        boolean actual = MCPDatabaseDialect.of(databaseType).isInformationSchemaColumnSchemaFilterRequired();
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertIsSystemSchema() {
        boolean actual = MCPDatabaseDialect.of("PostgreSQL").isSystemSchema("PG_CATALOG");
        assertTrue(actual);
    }
    
    @Test
    void assertIsSystemSchemaWithCatalog() {
        boolean actual = MCPDatabaseDialect.of("MySQL").isSystemSchema("", "information_schema", SchemaSemantics.DATABASE_AS_SCHEMA);
        assertTrue(actual);
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
                Arguments.of("mysql", "MySQL", QuoteCharacter.BACK_QUOTE),
                Arguments.of("hive", "Hive", QuoteCharacter.BACK_QUOTE),
                Arguments.of("sql server", "SQLServer", QuoteCharacter.BRACKETS),
                Arguments.of("postgresql", "PostgreSQL", QuoteCharacter.QUOTE),
                Arguments.of("unknown", "FixtureDB", QuoteCharacter.QUOTE));
    }
    
    private static Stream<Arguments> isUnquotedIdentifierCaseFoldedArguments() {
        return Stream.of(
                Arguments.of("postgresql", "PostgreSQL", true),
                Arguments.of("open gauss", "openGauss", true),
                Arguments.of("mysql", "MySQL", false),
                Arguments.of("unknown", "FixtureDB", false));
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
    
    private static Stream<Arguments> isInformationSchemaColumnSchemaFilterRequiredArguments() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", true),
                Arguments.of("postgresql", "PostgreSQL", true),
                Arguments.of("sql server", "SQLServer", false),
                Arguments.of("unknown", "FixtureDB", false));
    }
}
