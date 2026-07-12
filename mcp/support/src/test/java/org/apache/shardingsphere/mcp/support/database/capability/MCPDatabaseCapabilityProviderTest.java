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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
        assertThat(actual.get().getDefaultSchemaSemantics(), is(DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertFalse(actual.get().isSupportsCrossSchemaSql());
        assertTrue(actual.get().isSupportsExplain());
        assertFalse(actual.get().getIdentifierCasePolicy().matches("phone", "Phone", QuoteCharacter.NONE));
    }
    
    @Test
    void assertProvideWithoutSequence() {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider().provide("warehouse");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX));
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.SEQUENCE));
        assertThat(actual.get().getTransactionCapability(), is(TransactionCapability.NONE));
        assertFalse(actual.get().isSupportsTransactionControl());
        assertFalse(actual.get().isSupportsSavepoint());
        assertThat(actual.get().getDefaultSchemaSemantics(), is(DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
    }
    
    @Test
    void assertProvideWithoutCapabilityOption() {
        MCPDatabaseCapabilityProvider provider = createCapabilityProvider("FixtureDB", "",
                new CapabilityFixture(false, false, false, DialectSchemaSemantics.NATIVE_SCHEMA));
        assertThat(provider.findDatabaseProfile("logic_db").orElseThrow().getDatabaseType(), is("FixtureDB"));
        assertFalse(provider.provide("logic_db").isPresent());
    }
    
    @Test
    void assertResolveIdentifierCasePolicyPerDatabase() throws SQLException {
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);
        RuntimeDatabaseConfiguration firstRuntimeDatabase = createRuntimeDatabaseConfiguration("first_db", "MySQL", "", true, true, firstConnection);
        RuntimeDatabaseConfiguration secondRuntimeDatabase = createRuntimeDatabaseConfiguration("second_db", "MySQL", "", true, true, secondConnection);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(2, 1F);
        runtimeDatabases.put("first_db", firstRuntimeDatabase);
        runtimeDatabases.put("second_db", secondRuntimeDatabase);
        Map<Connection, IdentifierCasePolicySet> policies = Map.of(
                firstConnection, IdentifierCasePolicyFactory.newSensitivePolicySet(), secondConnection, IdentifierCasePolicyFactory.newInsensitivePolicySet());
        MCPDatabaseCapabilityProvider provider = createCapabilityProvider(runtimeDatabases,
                Map.of("MySQL", new CapabilityFixture(true, true, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA)), invocation -> {
                    try (Connection connection = invocation.getArgument(2, DataSource.class).getConnection()) {
                        return policies.get(connection);
                    }
                });
        assertFalse(provider.provide("first_db").orElseThrow().getIdentifierCasePolicy().matches("phone", "Phone", QuoteCharacter.NONE));
        assertTrue(provider.provide("second_db").orElseThrow().getIdentifierCasePolicy().matches("phone", "Phone", QuoteCharacter.NONE));
        verify(firstRuntimeDatabase, times(2)).openConnection("first_db");
        verify(firstRuntimeDatabase, never()).openConnection("second_db");
        verify(secondRuntimeDatabase, times(2)).openConnection("second_db");
        verify(secondRuntimeDatabase, never()).openConnection("first_db");
    }
    
    @Test
    void assertResolveIdentifierCasePolicyWhenRuntimeConnectionFails() throws SQLException {
        SQLException connectionFailure = new SQLException("connection unavailable");
        Connection profileConnection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabase = createRuntimeDatabaseConfiguration("logic_db", "MySQL", "", true, true, profileConnection);
        when(runtimeDatabase.openConnection("logic_db")).thenReturn(profileConnection)
                .thenThrow(RuntimeDatabaseConnectionException.connectionFailed("logic_db", connectionFailure));
        MCPDatabaseCapabilityProvider provider = createCapabilityProvider(Map.of("logic_db", runtimeDatabase),
                Map.of("MySQL", new CapabilityFixture(true, true, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA)), invocation -> {
                    try (Connection ignored = invocation.getArgument(2, DataSource.class).getConnection()) {
                        return IdentifierCasePolicyFactory.newSensitivePolicySet();
                    } catch (final SQLException ex) {
                        assertThat(ex, is(connectionFailure));
                        return IdentifierCasePolicyFactory.newInsensitivePolicySet();
                    }
                });
        assertTrue(provider.provide("logic_db").orElseThrow().getIdentifierCasePolicy().matches("phone", "Phone", QuoteCharacter.NONE));
        verify(runtimeDatabase, times(2)).openConnection("logic_db");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCapabilityMatrixArguments")
    void assertProvideWithCapabilityMatrix(final String name, final String databaseType, final boolean expectedTransactionControl,
                                           final boolean expectedSavepoint, final boolean expectedSequenceSupport,
                                           final SchemaExecutionSemantics expectedSchemaExecutionSemantics, final boolean expectedExplainSupport) {
        CapabilityFixture capabilityFixture = new CapabilityFixture(expectedTransactionControl, expectedSavepoint, expectedSequenceSupport,
                SchemaExecutionSemantics.FIXED_TO_DATABASE == expectedSchemaExecutionSemantics ? DialectSchemaSemantics.DATABASE_AS_SCHEMA : DialectSchemaSemantics.NATIVE_SCHEMA);
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider(databaseType, "", capabilityFixture).provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().isSupportsTransactionControl(), is(expectedTransactionControl));
        assertThat(actual.get().isSupportsSavepoint(), is(expectedSavepoint));
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX));
        assertThat(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.SEQUENCE), is(expectedSequenceSupport));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(expectedSchemaExecutionSemantics));
        assertThat(actual.get().isSupportsCrossSchemaSql(), is(SchemaExecutionSemantics.BEST_EFFORT == expectedSchemaExecutionSemantics));
        assertThat(actual.get().isSupportsExplain(), is(expectedExplainSupport));
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider() {
        Map<String, CapabilityFixture> capabilityFixtures = Map.of(
                "MySQL", new CapabilityFixture(true, true, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA),
                "Hive", new CapabilityFixture(false, false, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        return createCapabilityProvider(createRuntimeDatabases(Map.of("logic_db", "MySQL", "warehouse", "Hive"), capabilityFixtures), capabilityFixtures);
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider(final String databaseType, final String databaseVersion, final CapabilityFixture capabilityFixture) {
        return createCapabilityProvider(Map.of("logic_db", createRuntimeDatabaseConfiguration("logic_db", databaseType, databaseVersion,
                capabilityFixture.transactionSupported, capabilityFixture.savepointSupported)),
                Map.of(databaseType, capabilityFixture));
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases,
                                                                   final Map<String, CapabilityFixture> capabilityFixtures) {
        return createCapabilityProvider(runtimeDatabases, capabilityFixtures, invocation -> {
            try (Connection ignored = invocation.getArgument(2, DataSource.class).getConnection()) {
                return IdentifierCasePolicyFactory.newSensitivePolicySet();
            }
        });
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases,
                                                                   final Map<String, CapabilityFixture> capabilityFixtures,
                                                                   final Answer<IdentifierCasePolicySet> identifierCasePolicyResolverAnswer) {
        try (
                MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<IdentifierCasePolicyResolver> ignoredResolver =
                        mockConstruction(IdentifierCasePolicyResolver.class,
                                (mock, context) -> when(mock.resolve(any(), any(), any())).thenAnswer(identifierCasePolicyResolverAnswer))) {
            for (Entry<String, CapabilityFixture> entry : capabilityFixtures.entrySet()) {
                mockDatabaseType(entry.getKey(), entry.getValue(), typedSPILoader, databaseTypedSPILoader);
            }
            return new MCPDatabaseCapabilityProvider(runtimeDatabases);
        }
    }
    
    private void mockDatabaseType(final String databaseType, final CapabilityFixture capabilityFixture, final MockedStatic<TypedSPILoader> typedSPILoader,
                                  final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getType()).thenReturn(databaseType);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(
                new DefaultSchemaOption(false, null, capabilityFixture.schemaSemantics));
        when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(
                capabilityFixture.sequenceSupported ? Optional.of(new DialectSequenceOption("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM TEST_SEQUENCES")) : Optional.empty());
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(Optional.of(dialectDatabaseMetaData));
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final Map<String, String> databaseTypes, final Map<String, CapabilityFixture> capabilityFixtures) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(databaseTypes.size(), 1F);
        for (Entry<String, String> entry : databaseTypes.entrySet()) {
            CapabilityFixture capabilityFixture = capabilityFixtures.get(entry.getValue());
            result.put(entry.getKey(), createRuntimeDatabaseConfiguration(entry.getKey(), entry.getValue(), "",
                    capabilityFixture.transactionSupported, capabilityFixture.savepointSupported));
        }
        return result;
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseName, final String databaseType, final String databaseVersion,
                                                                            final boolean transactionSupported, final boolean savepointSupported) {
        return createRuntimeDatabaseConfiguration(databaseName, databaseType, databaseVersion, transactionSupported, savepointSupported, mock(Connection.class));
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseName, final String databaseType, final String databaseVersion,
                                                                            final boolean transactionSupported, final boolean savepointSupported, final Connection connection) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        try {
            DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
            Statement statement = mock(Statement.class);
            ResultSet scalarResultSet = mock(ResultSet.class);
            when(result.openConnection(databaseName)).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(scalarResultSet);
            when(scalarResultSet.next()).thenReturn(false);
            when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseVersion);
            when(databaseMetaData.supportsTransactions()).thenReturn(transactionSupported);
            when(databaseMetaData.supportsSavepoints()).thenReturn(savepointSupported);
            when(databaseMetaData.getURL()).thenReturn(SupportDatabaseTypeFactoryMocker.createJdbcUrl(databaseType));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    private static Stream<Arguments> provideCapabilityMatrixArguments() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", true, true, false, SchemaExecutionSemantics.FIXED_TO_DATABASE, true),
                Arguments.of("postgresql", "PostgreSQL", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, true),
                Arguments.of("open gauss", "openGauss", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, true),
                Arguments.of("sql server", "SQLServer", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, false),
                Arguments.of("mariadb", "MariaDB", true, true, true, SchemaExecutionSemantics.FIXED_TO_DATABASE, true),
                Arguments.of("oracle", "Oracle", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, false),
                Arguments.of("clickhouse", "ClickHouse", false, false, false, SchemaExecutionSemantics.FIXED_TO_DATABASE, false),
                Arguments.of("hive", "Hive", false, false, false, SchemaExecutionSemantics.FIXED_TO_DATABASE, false),
                Arguments.of("presto", "Presto", true, false, false, SchemaExecutionSemantics.BEST_EFFORT, true),
                Arguments.of("firebird", "Firebird", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, false));
    }
    
    private static final class CapabilityFixture {
        
        private final boolean transactionSupported;
        
        private final boolean savepointSupported;
        
        private final boolean sequenceSupported;
        
        private final DialectSchemaSemantics schemaSemantics;
        
        private CapabilityFixture(final boolean transactionSupported, final boolean savepointSupported, final boolean sequenceSupported,
                                  final DialectSchemaSemantics schemaSemantics) {
            this.transactionSupported = transactionSupported;
            this.savepointSupported = savepointSupported;
            this.sequenceSupported = sequenceSupported;
            this.schemaSemantics = schemaSemantics;
        }
    }
}
