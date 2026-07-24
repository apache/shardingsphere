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

package org.apache.shardingsphere.mcp.support.database.tool.service;

import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.tool.request.RuntimeDatabaseValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RuntimeDatabaseValidationServiceTest {
    
    private MockedConstruction<MCPJdbcDatabaseProfileLoader> mockedProfileLoaders;
    
    private MockedConstruction<MCPJdbcMetadataLoader> mockedMetadataLoaders;
    
    @BeforeEach
    void setUp() {
        mockedProfileLoaders = mockConstruction(MCPJdbcDatabaseProfileLoader.class);
        mockedMetadataLoaders = mockConstruction(MCPJdbcMetadataLoader.class);
    }
    
    @AfterEach
    void tearDown() {
        mockedMetadataLoaders.close();
        mockedProfileLoaders.close();
    }
    
    @Test
    void assertValidateWithMissingDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest(""), ignored -> Optional.empty());
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getDatabase(), is(""));
        assertThat(actual.getCategory(), is("invalid_configuration"));
        assertThat(actual.getChecks().get(0).getName(), is("configuration"));
        assertThat(actual.getChecks().get(1).getStatus(), is("skipped"));
        verifyNoInteractions(getProfileLoader(), getMetadataLoader());
    }
    
    @Test
    void assertValidateWithUnknownDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.empty());
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getCategory(), is("invalid_configuration"));
        assertThat(actual.getChecks().get(0).getStatus(), is("failed"));
        verifyNoInteractions(getProfileLoader(), getMetadataLoader());
    }
    
    @Test
    void assertValidateWithConfiguredDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration();
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(createProfile());
        when(getMetadataLoader().load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("logic_db"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getStatus(), is("ready"));
        ArgumentCaptor<RuntimeDatabaseConfiguration> configurationCaptor = ArgumentCaptor.forClass(RuntimeDatabaseConfiguration.class);
        verify(profileLoader).load(any(), configurationCaptor.capture());
        assertThat(configurationCaptor.getValue(), is(runtimeDatabaseConfig));
    }
    
    @Test
    void assertValidateWithVisibleDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        MCPJdbcMetadataLoader metadataLoader = getMetadataLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration();
        IdentifierCasePolicySet identifierCasePolicySet = new IdentifierCasePolicySet(
                IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.TABLE),
                Map.of(IdentifierScope.SCHEMA, IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.SCHEMA)));
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class)))
                .thenReturn(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                        new DatabaseIdentifierContext(identifierCasePolicySet)));
        when(metadataLoader.load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("Logic_DB"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getStatus(), is("ready"));
        assertThat(actual.getChecks().get(4).getStatus(), is("passed"));
    }
    
    @Test
    void assertValidateWithInvisibleDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        MCPJdbcMetadataLoader metadataLoader = getMetadataLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = new RuntimeDatabaseConfiguration(InvisibleDatabaseDriver.JDBC_URL, "demo", "", InvisibleDatabaseDriver.class.getName());
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(
                new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                        new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newSensitivePolicySet())));
        when(metadataLoader.load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("Logic_DB"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE));
        assertThat(actual.getChecks().get(4).getStatus(), is("failed"));
    }
    
    @Test
    void assertValidateWithVisibleCatalog() throws SQLException {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("Logic_DB");
        IdentifierCasePolicySet identifierCasePolicySet = new IdentifierCasePolicySet(
                IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.TABLE),
                Map.of(IdentifierScope.SCHEMA, IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.SCHEMA)));
        when(getProfileLoader().load(any(), any(RuntimeDatabaseConfiguration.class)))
                .thenReturn(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                        new DatabaseIdentifierContext(identifierCasePolicySet)));
        when(getMetadataLoader().load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("public"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getStatus(), is("ready"));
    }
    
    @Test
    void assertValidateVisibilityWithDialectFailure() throws SQLException {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        when(runtimeDatabaseConfig.openConnection("logic_db")).thenReturn(connection);
        when(connection.getCatalog()).thenThrow(new SQLException("permission denied", "28000", 335544352));
        when(getProfileLoader().load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(
                new RuntimeDatabaseProfile("logic_db", "Firebird", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                        new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newSensitivePolicySet())));
        when(getMetadataLoader().load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("public"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
        assertThat(actual.getChecks().get(4).getStatus(), is("failed"));
    }
    
    @Test
    void assertValidateWithMetadataReadFailure() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        MCPJdbcMetadataLoader metadataLoader = getMetadataLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration();
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(createProfile());
        when(metadataLoader.load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class)))
                .thenThrow(RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("Broken connection")));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED));
        assertThat(actual.getChecks().get(3).getStatus(), is("failed"));
        assertThat(actual.getChecks().get(4).getStatus(), is("skipped"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertValidateFailsDuringProfileLoadCases")
    void assertValidateFailsDuringProfileLoad(final String name, final RuntimeDatabaseConnectionException cause, final String expectedDriverStatus,
                                              final String expectedConnectivityStatus, final String expectedCategory) {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration();
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenThrow(cause);
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"), ignored -> Optional.of(runtimeDatabaseConfig));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getCategory(), is(expectedCategory));
        assertThat(actual.getChecks().get(1).getStatus(), is(expectedDriverStatus));
        assertThat(actual.getChecks().get(2).getStatus(), is(expectedConnectivityStatus));
        assertThat(actual.getChecks().get(3).getStatus(), is("skipped"));
        assertThat(actual.getChecks().get(4).getStatus(), is("skipped"));
        verifyNoInteractions(getMetadataLoader());
    }
    
    private MCPJdbcDatabaseProfileLoader getProfileLoader() {
        return mockedProfileLoaders.constructed().getFirst();
    }
    
    private MCPJdbcMetadataLoader getMetadataLoader() {
        return mockedMetadataLoaders.constructed().getFirst();
    }
    
    private static Stream<Arguments> assertValidateFailsDuringProfileLoadCases() {
        return Stream.of(
                Arguments.of("missing driver", RuntimeDatabaseConnectionException.missingJdbcDriver("logic_db", new ClassNotFoundException("missing")), "failed", "skipped",
                        RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER),
                Arguments.of("authentication failed", RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("Access denied", "28000")), "passed", "failed",
                        RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED),
                Arguments.of("authorization failed", RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("permission denied", "42501")), "passed", "failed",
                        RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED),
                Arguments.of("connection timeout", RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLTimeoutException("timed out")), "passed", "failed",
                        RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT),
                Arguments.of("database type mismatch", RuntimeDatabaseConnectionException.invalidConfiguration("logic_db", new IllegalStateException("mismatch")), "passed", "failed",
                        RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
    }
    
    private static RuntimeDatabaseProfile createProfile() {
        return new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()));
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration() {
        return new RuntimeDatabaseConfiguration("jdbc:test:profile", "demo", "", "com.mysql.cj.jdbc.Driver");
    }
    
    private static List<ShardingSphereSchema> createMetadata(final String schemaName) {
        return List.of(new ShardingSphereSchema(schemaName, mock(DatabaseType.class)));
    }
    
    private static final class InvisibleDatabaseDriver implements Driver {
        
        private static final String JDBC_URL = "jdbc:runtime-validation:invisible";
        
        private static final InvisibleDatabaseDriver INSTANCE = new InvisibleDatabaseDriver();
        
        static {
            try {
                DriverManager.registerDriver(INSTANCE);
            } catch (final SQLException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
        
        @Override
        public Connection connect(final String url, final Properties info) throws SQLException {
            if (!acceptsURL(url)) {
                return null;
            }
            Connection result = mock(Connection.class);
            DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
            ResultSet catalogs = mock(ResultSet.class);
            ResultSet schemas = mock(ResultSet.class);
            when(result.getCatalog()).thenReturn("");
            when(result.getSchema()).thenReturn("");
            when(result.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getCatalogs()).thenReturn(catalogs);
            when(databaseMetaData.getSchemas()).thenReturn(schemas);
            when(catalogs.next()).thenReturn(false);
            when(schemas.next()).thenReturn(false);
            return result;
        }
        
        @Override
        public boolean acceptsURL(final String url) {
            return JDBC_URL.equals(url);
        }
        
        @Override
        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
            return new DriverPropertyInfo[0];
        }
        
        @Override
        public int getMajorVersion() {
            return 1;
        }
        
        @Override
        public int getMinorVersion() {
            return 0;
        }
        
        @Override
        public boolean jdbcCompliant() {
            return false;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("Invisible database driver does not expose a parent logger.");
        }
    }
}
