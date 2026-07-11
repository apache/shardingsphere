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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.tool.request.RuntimeDatabaseValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.RuntimeDatabaseValidationResult;
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
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest(""),
                ignored -> Optional.empty(), RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("status"), is("failed"));
        assertThat(actualPayload.get("category"), is("invalid_configuration"));
        assertThat(actualPayload.get("recovery"), is(Map.of("category", "invalid_configuration")));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(0)).get("name"), is("configuration"));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(1)).get("status"), is("skipped"));
        verifyNoInteractions(getProfileLoader(), getMetadataLoader());
    }
    
    @Test
    void assertValidateWithUnknownDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"),
                ignored -> Optional.empty(), RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("status"), is("failed"));
        assertThat(actualPayload.get("category"), is("invalid_configuration"));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(0)).get("status"), is("failed"));
        verifyNoInteractions(getProfileLoader(), getMetadataLoader());
    }
    
    @Test
    void assertValidateWithConfiguredDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration();
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(createProfile());
        when(getMetadataLoader().load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("logic_db"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"),
                ignored -> Optional.of(runtimeDatabaseConfig),
                RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("status"), is("ready"));
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
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(createProfile());
        when(metadataLoader.load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("logic_db"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"),
                ignored -> Optional.of(runtimeDatabaseConfig), RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("status"), is("ready"));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(4)).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWithInvisibleDatabase() {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        MCPJdbcMetadataLoader metadataLoader = getMetadataLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = new RuntimeDatabaseConfiguration(InvisibleDatabaseDriver.JDBC_URL, "demo", "", InvisibleDatabaseDriver.class.getName());
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenReturn(createProfile());
        when(metadataLoader.load(any(), any(RuntimeDatabaseConfiguration.class), any(RuntimeDatabaseProfile.class))).thenReturn(createMetadata("public"));
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"),
                ignored -> Optional.of(runtimeDatabaseConfig), RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("status"), is("failed"));
        assertThat(actualPayload.get("category"), is(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE));
        assertThat(actualPayload.get("recovery"), is(Map.of("category", RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE)));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(4)).get("status"), is("failed"));
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
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"),
                ignored -> Optional.of(runtimeDatabaseConfig), RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("status"), is("failed"));
        assertThat(actualPayload.get("category"), is(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(3)).get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("checks")).get(4)).get("status"), is("skipped"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertValidateFailsDuringProfileLoadCases")
    void assertValidateFailsDuringProfileLoad(final String name, final RuntimeDatabaseConnectionException cause, final String expectedDriverStatus,
                                              final String expectedConnectivityStatus, final String expectedCategory) {
        RuntimeDatabaseValidationService service = new RuntimeDatabaseValidationService();
        MCPJdbcDatabaseProfileLoader profileLoader = getProfileLoader();
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = createRuntimeDatabaseConfiguration();
        when(profileLoader.load(any(), any(RuntimeDatabaseConfiguration.class))).thenThrow(cause);
        RuntimeDatabaseValidationResult actual = service.validate(new RuntimeDatabaseValidationRequest("logic_db"),
                ignored -> Optional.of(runtimeDatabaseConfig), RuntimeDatabaseValidationServiceTest::createRecoveryPayload);
        Map<String, Object> actualPayload = actual.toPayload();
        List<?> checks = (List<?>) actualPayload.get("checks");
        assertThat(actualPayload.get("status"), is("failed"));
        assertThat(actualPayload.get("category"), is(expectedCategory));
        assertThat(((Map<?, ?>) checks.get(1)).get("status"), is(expectedDriverStatus));
        assertThat(((Map<?, ?>) checks.get(2)).get("status"), is(expectedConnectivityStatus));
        assertThat(((Map<?, ?>) checks.get(3)).get("status"), is("skipped"));
        assertThat(((Map<?, ?>) checks.get(4)).get("status"), is("skipped"));
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
        return new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", true, true);
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration() {
        return new RuntimeDatabaseConfiguration("jdbc:test:profile", "demo", "", "com.mysql.cj.jdbc.Driver");
    }
    
    private static List<ShardingSphereSchema> createMetadata(final String schemaName) {
        return List.of(new ShardingSphereSchema(schemaName, mock(DatabaseType.class)));
    }
    
    private static Map<String, Object> createRecoveryPayload(final RuntimeDatabaseConnectionException cause) {
        return Map.of("category", cause.getCategory());
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
