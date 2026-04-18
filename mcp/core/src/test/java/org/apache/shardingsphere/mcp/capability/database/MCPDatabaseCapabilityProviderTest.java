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

package org.apache.shardingsphere.mcp.capability.database;

import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPDatabaseCapabilityProviderTest {

    @Test
    void assertProvide() {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider().provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is("logic_db"));
        assertThat(actual.get().getDatabaseType(), is("MySQL"));
        assertThat(actual.get().getSupportedMetadataObjectTypes(),
                is(EnumSet.of(SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN,
                        SupportedMCPMetadataObjectType.INDEX)));
        assertThat(actual.get().getTransactionCapability(), is(TransactionCapability.LOCAL_WITH_SAVEPOINT));
        assertTrue(actual.get().isSupportsTransactionControl());
        assertTrue(actual.get().isSupportsSavepoint());
        assertThat(actual.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertFalse(actual.get().isSupportsCrossSchemaSql());
        assertFalse(actual.get().isSupportsExplainAnalyze());
    }

    @Test
    void assertProvideWithoutIndex() {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider().provide("warehouse");
        assertTrue(actual.isPresent());
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX));
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.SEQUENCE));
        assertThat(actual.get().getTransactionCapability(), is(TransactionCapability.NONE));
        assertFalse(actual.get().isSupportsTransactionControl());
        assertFalse(actual.get().isSupportsSavepoint());
        assertThat(actual.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCapabilityMatrixArguments")
    void assertProvideWithCapabilityMatrix(final String name, final String databaseType, final boolean expectedTransactionControl,
                                           final boolean expectedSavepoint, final boolean expectedIndexSupport, final boolean expectedSequenceSupport,
                                           final SchemaExecutionSemantics expectedSchemaExecutionSemantics) {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider(databaseType).provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().isSupportsTransactionControl(), is(expectedTransactionControl));
        assertThat(actual.get().isSupportsSavepoint(), is(expectedSavepoint));
        assertThat(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX), is(expectedIndexSupport));
        assertThat(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.SEQUENCE), is(expectedSequenceSupport));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(expectedSchemaExecutionSemantics));
    }

    @Test
    void assertProvideWithRuntimeOverlay() {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider("MySQL", "8.0.32").provide("logic_db");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertFalse(actual.get().isSupportsCrossSchemaSql());
        assertTrue(actual.get().isSupportsExplainAnalyze());
    }

    private MCPDatabaseCapabilityProvider createCapabilityProvider() {
        return new MCPDatabaseCapabilityProvider(createRuntimeDatabases(Map.of("logic_db", "MySQL", "warehouse", "Hive")));
    }

    private MCPDatabaseCapabilityProvider createCapabilityProvider(final String databaseType) {
        return createCapabilityProvider(databaseType, "");
    }

    private MCPDatabaseCapabilityProvider createCapabilityProvider(final String databaseType, final String databaseVersion) {
        return new MCPDatabaseCapabilityProvider(Map.of("logic_db", createRuntimeDatabaseConfiguration("logic_db", databaseType, databaseVersion)));
    }

    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final Map<String, String> databaseTypes) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(databaseTypes.size(), 1F);
        for (Entry<String, String> entry : databaseTypes.entrySet()) {
            result.put(entry.getKey(), createRuntimeDatabaseConfiguration(entry.getKey(), entry.getValue(), ""));
        }
        return result;
    }

    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseName, final String databaseType, final String databaseVersion) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        try {
            Connection connection = mock(Connection.class);
            DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
            when(result.getDatabaseType()).thenReturn(databaseType);
            when(result.openConnection(databaseName)).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseType);
            when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseVersion);
            when(databaseMetaData.getURL()).thenReturn(String.format("jdbc:%s:test", databaseType.toLowerCase(Locale.ENGLISH)));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }

    private static Stream<Arguments> provideCapabilityMatrixArguments() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", true, true, true, false, SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Arguments.of("postgresql", "PostgreSQL", true, true, true, true, SchemaExecutionSemantics.BEST_EFFORT),
                Arguments.of("open gauss", "openGauss", true, true, true, true, SchemaExecutionSemantics.BEST_EFFORT),
                Arguments.of("sql server", "SQLServer", true, true, true, true, SchemaExecutionSemantics.BEST_EFFORT),
                Arguments.of("mariadb", "MariaDB", true, true, true, true, SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Arguments.of("oracle", "Oracle", true, true, true, true, SchemaExecutionSemantics.BEST_EFFORT),
                Arguments.of("clickhouse", "ClickHouse", false, false, false, false, SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Arguments.of("doris", "Doris", true, false, true, false, SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Arguments.of("hive", "Hive", false, false, false, false, SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Arguments.of("presto", "Presto", true, false, false, false, SchemaExecutionSemantics.BEST_EFFORT),
                Arguments.of("firebird", "Firebird", true, true, true, true, SchemaExecutionSemantics.BEST_EFFORT),
                Arguments.of("h2", "H2", true, true, true, true, SchemaExecutionSemantics.BEST_EFFORT));
    }
}
