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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPJdbcMetadataLoaderSequenceTest extends AbstractMCPJdbcMetadataLoaderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadWithoutSequenceQueryArguments")
    void assertLoadWithoutSequenceQuery(final String name, final String databaseType) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(createConnectionWithoutSchema(databaseType));
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }
    
    @Test
    void assertLoadWithoutSystemSchemaSequence() throws SQLException {
        Connection connection = createConnectionWithMetadata("PostgreSQL", List.of(), List.of(), Map.of(), Map.of(),
                List.of(Map.of("SEQUENCE_SCHEMA", "PG_CATALOG", "SEQUENCE_NAME", "order_seq")));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }
    
    @Test
    void assertLoadWithoutEmptySequenceName() throws SQLException {
        Connection connection = createConnectionWithMetadata("PostgreSQL", List.of(), List.of(), Map.of(), Map.of(),
                List.of(Map.of("SEQUENCE_SCHEMA", "public", "SEQUENCE_NAME", "")));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, ""));
    }
    
    @Test
    void assertLoadWithFailedSequenceMetadataQuery() throws SQLException {
        Driver mockDriver = new MockDriver("jdbc:mock:failed-sequence-query", createConnectionWithFailedSequenceMetadataQuery());
        try (MockDriverRegistration ignored = MockDriverRegistration.register(mockDriver)) {
            RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                    () -> load(Map.of("logic_db", new RuntimeDatabaseConfiguration("jdbc:mock:failed-sequence-query", "", "", MockDriver.class.getName()))));
            assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: connection_failed."));
            assertThat(actual.getCause().getMessage(), is("sequence metadata query failed"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadSequenceDialectArguments")
    void assertLoadWithDialectSequenceMetadata(final String name, final String databaseType, final String sequenceSchema,
                                               final String sequenceName, final String sequenceQuery) throws SQLException {
        String jdbcUrl = "jdbc:mock:sequence:" + databaseType.toLowerCase(Locale.ENGLISH);
        Driver mockDriver = new MockDriver(jdbcUrl, createConnectionWithSequenceMetadata(databaseType, sequenceSchema, sequenceName, sequenceQuery));
        try (MockDriverRegistration ignored = MockDriverRegistration.register(mockDriver)) {
            LoadedMetadataCatalog actual = load(Map.of("logic_db", new RuntimeDatabaseConfiguration(jdbcUrl, "", "", MockDriver.class.getName())));
            assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), SupportedMCPMetadataObjectType.SEQUENCE, sequenceName));
        }
    }
}
