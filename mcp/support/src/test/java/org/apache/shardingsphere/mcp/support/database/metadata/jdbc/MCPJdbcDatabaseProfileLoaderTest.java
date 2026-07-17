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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPJdbcDatabaseProfileLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        IdentifierCasePolicySet expectedIdentifierCasePolicySet = IdentifierCasePolicyFactory.newSensitivePolicySet();
        try (
                MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
                MockedStatic<IdentifierCasePolicyResolver> ignoredResolver = mockStatic(IdentifierCasePolicyResolver.class)) {
            ignoredResolver.when(() -> IdentifierCasePolicyResolver.resolveStorage(any(), any(), any())).thenReturn(expectedIdentifierCasePolicySet);
            RuntimeDatabaseProfile actual =
                    new MCPJdbcDatabaseProfileLoader().load("logic_db", createRuntimeDatabaseConfiguration(SupportDatabaseTypeFactoryMocker.createJdbcUrl("FixtureDB"), "1.0", true, true));
            assertThat(actual.getDatabase(), is("logic_db"));
            assertThat(actual.getDatabaseType(), is("FixtureDB"));
            assertThat(actual.getDatabaseVersion(), is("1.0"));
            assertThat(actual.getTransactionCapability(), is(TransactionCapability.LOCAL_WITH_SAVEPOINT));
            assertThat(actual.getIdentifierCasePolicySet(), is(expectedIdentifierCasePolicySet));
        }
    }
    
    @Test
    void assertLoadWithoutTransaction() throws SQLException {
        Connection connection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration(
                SupportDatabaseTypeFactoryMocker.createJdbcUrl("FixtureDB"), "1.0", false, true, connection);
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        try (MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata()) {
            RuntimeDatabaseProfile actual = new MCPJdbcDatabaseProfileLoader().load("logic_db", runtimeDatabaseConfig);
            assertThat(actual.getTransactionCapability(), is(TransactionCapability.NONE));
            verify(databaseMetaData, never()).supportsSavepoints();
        }
    }
    
    @Test
    void assertLoadWithoutSavepoint() throws SQLException {
        try (MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata()) {
            RuntimeDatabaseProfile actual =
                    new MCPJdbcDatabaseProfileLoader().load("logic_db", createRuntimeDatabaseConfiguration(SupportDatabaseTypeFactoryMocker.createJdbcUrl("FixtureDB"), "1.0", true, false));
            assertThat(actual.getTransactionCapability(), is(TransactionCapability.LOCAL));
        }
    }
    
    @Test
    void assertLoadIdentifierCasePolicyPerDatabase() throws SQLException {
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);
        RuntimeDatabaseConfiguration firstRuntimeDatabase = createRuntimeDatabaseConfiguration(
                SupportDatabaseTypeFactoryMocker.createJdbcUrl("FixtureDB"), "1.0", true, true, firstConnection);
        RuntimeDatabaseConfiguration secondRuntimeDatabase = createRuntimeDatabaseConfiguration(
                SupportDatabaseTypeFactoryMocker.createJdbcUrl("FixtureDB"), "1.0", true, true, secondConnection);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(2, 1F);
        runtimeDatabases.put("first_db", firstRuntimeDatabase);
        runtimeDatabases.put("second_db", secondRuntimeDatabase);
        Map<Connection, IdentifierCasePolicySet> policies = Map.of(
                firstConnection, IdentifierCasePolicyFactory.newSensitivePolicySet(), secondConnection, IdentifierCasePolicyFactory.newInsensitivePolicySet());
        try (
                MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
                MockedStatic<IdentifierCasePolicyResolver> ignoredResolver = mockStatic(IdentifierCasePolicyResolver.class)) {
            ignoredResolver.when(() -> IdentifierCasePolicyResolver.resolveStorage(any(), any(), any())).thenAnswer(invocation -> {
                try (Connection connection = invocation.getArgument(2, DataSource.class).getConnection()) {
                    return policies.get(connection);
                }
            });
            Map<String, RuntimeDatabaseProfile> actual = new MCPJdbcDatabaseProfileLoader().load(runtimeDatabases);
            assertFalse(actual.get("first_db").getIdentifierCasePolicySet().getPolicy(IdentifierScope.TABLE).matches("phone", "Phone", QuoteCharacter.NONE));
            assertTrue(actual.get("second_db").getIdentifierCasePolicySet().getPolicy(IdentifierScope.TABLE).matches("phone", "Phone", QuoteCharacter.NONE));
            verify(firstRuntimeDatabase, times(2)).openConnection("first_db");
            verify(firstRuntimeDatabase, never()).openConnection("second_db");
            verify(secondRuntimeDatabase, times(2)).openConnection("second_db");
            verify(secondRuntimeDatabase, never()).openConnection("first_db");
        }
    }
    
    @Test
    void assertLoadWhenIdentifierCasePolicyConnectionFails() throws SQLException {
        SQLException connectionFailure = new SQLException("connection unavailable");
        Connection profileConnection = mock(Connection.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration(
                SupportDatabaseTypeFactoryMocker.createJdbcUrl("FixtureDB"), "1.0", true, true, profileConnection);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(profileConnection)
                .thenThrow(RuntimeDatabaseConnectionException.connectionFailed("logic_db", connectionFailure));
        try (
                MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
                MockedStatic<IdentifierCasePolicyResolver> ignoredResolver = mockStatic(IdentifierCasePolicyResolver.class)) {
            ignoredResolver.when(() -> IdentifierCasePolicyResolver.resolveStorage(any(), any(), any())).thenAnswer(invocation -> {
                try (Connection ignoredConnection = invocation.getArgument(2, DataSource.class).getConnection()) {
                    return IdentifierCasePolicyFactory.newSensitivePolicySet();
                } catch (final SQLException ex) {
                    assertThat(ex, is(connectionFailure));
                    return IdentifierCasePolicyFactory.newInsensitivePolicySet();
                }
            });
            RuntimeDatabaseProfile actual = new MCPJdbcDatabaseProfileLoader().load("logic_db", runtimeDatabaseConfig);
            assertTrue(actual.getIdentifierCasePolicySet().getPolicy(IdentifierScope.TABLE).matches("phone", "Phone", QuoteCharacter.NONE));
            verify(runtimeDatabaseConfig, times(2)).openConnection("logic_db");
        }
    }
    
    @Test
    void assertLoadWithInvalidJdbcUrl() {
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            ShardingSphereExternalException expectedCause = mock(ShardingSphereExternalException.class);
            mocked.when(() -> DatabaseTypeFactory.get(any(DatabaseMetaData.class))).thenThrow(expectedCause);
            RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                    () -> new MCPJdbcDatabaseProfileLoader().load("logic_db", createRuntimeDatabaseConfiguration("jdbc:unknown:test", "8.0.32", true, true)));
            assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
            assertThat(actual.getCause(), is(expectedCause));
        }
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl, final String databaseVersion,
                                                                            final boolean supportsTransaction, final boolean supportsSavepoint) throws SQLException {
        return createRuntimeDatabaseConfiguration(jdbcUrl, databaseVersion, supportsTransaction, supportsSavepoint, mock(Connection.class));
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl, final String databaseVersion,
                                                                            final boolean supportsTransaction, final boolean supportsSavepoint,
                                                                            final Connection connection) throws SQLException {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(result.openConnection(anyString())).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseVersion);
        when(databaseMetaData.supportsTransactions()).thenReturn(supportsTransaction);
        when(databaseMetaData.supportsSavepoints()).thenReturn(supportsSavepoint);
        return result;
    }
}
