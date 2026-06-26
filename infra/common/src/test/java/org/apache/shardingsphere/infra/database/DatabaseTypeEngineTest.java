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

package org.apache.shardingsphere.infra.database;

import org.apache.shardingsphere.database.connector.core.exception.UnsupportedStorageTypeException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectJdbcUrlFetcher;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DatabaseTypeEngineTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getProtocolTypeWithDatabaseConfigurationArguments")
    void assertGetProtocolTypeWithDatabaseConfiguration(final String name,
                                                        final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final DatabaseType expectedDatabaseType) {
        assertThat(DatabaseTypeEngine.getProtocolType(databaseConfig, props), is(expectedDatabaseType));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getProtocolTypeWithDatabaseConfigurationsArguments")
    void assertGetProtocolTypeWithDatabaseConfigurations(final String name,
                                                         final Map<String, DatabaseConfiguration> databaseConfigs, final ConfigurationProperties props, final DatabaseType expectedDatabaseType) {
        assertThat(DatabaseTypeEngine.getProtocolType(databaseConfigs, props), is(expectedDatabaseType));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getStorageTypeArguments")
    void assertGetStorageType(final String name, final DataSource dataSource, final Collection<DialectJdbcUrlFetcher> fetchers, final DatabaseType expectedDatabaseType) {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(fetchers);
            assertThat(DatabaseTypeEngine.getStorageType(dataSource), is(expectedDatabaseType));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getStorageTypeWithSQLWrapperExceptionArguments")
    void assertGetStorageTypeWithSQLWrapperException(final String name,
                                                     final DataSource dataSource, final Collection<DialectJdbcUrlFetcher> fetchers, final Class<? extends SQLException> expectedCauseType) {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(fetchers);
            assertThat(assertThrows(SQLWrapperException.class, () -> DatabaseTypeEngine.getStorageType(dataSource)).getCause(), isA(expectedCauseType));
        }
    }
    
    @Test
    void assertGetStorageTypeWithUnsupportedStorageType() throws SQLException {
        Connection firstConnection = createConnectionWithUnsupportedUrl();
        Connection secondConnection = mock(Connection.class);
        DialectJdbcUrlFetcher fetcher = createDialectJdbcUrlFetcher(secondConnection, "jdbc:unsupported://localhost:3306/test");
        DataSource dataSource = createDataSource(firstConnection, secondConnection);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(fetcher));
            assertThrows(UnsupportedStorageTypeException.class, () -> DatabaseTypeEngine.getStorageType(dataSource));
        }
    }
    
    @Test
    void assertGetStorageTypeWithRuntimeException() throws SQLException {
        Connection firstConnection = createConnectionWithUnsupportedUrl();
        DataSource dataSource = createDataSource(firstConnection, null);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenThrow(new IllegalStateException("boom"));
            assertThat(assertThrows(IllegalStateException.class, () -> DatabaseTypeEngine.getStorageType(dataSource)).getMessage(), is("boom"));
        }
    }
    
    @Test
    void assertGetDefaultStorageType() {
        DatabaseType actual = DatabaseTypeEngine.getDefaultStorageType();
        assertThat(actual, is(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
    }
    
    private static Stream<Arguments> getProtocolTypeWithDatabaseConfigurationArguments() throws SQLException {
        return Stream.of(
                Arguments.of("configured_mysql", createDatabaseConfiguration(Collections.emptyMap()), createConfiguredProperties("MySQL"),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("configured_h2_trunk_mysql", createDatabaseConfiguration(Collections.emptyMap()), createConfiguredProperties("H2"),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("storage_unit_postgresql",
                        createDatabaseConfiguration(Collections.singletonMap("foo_ds", createDataSource(createConnectionWithUrl("jdbc:postgresql://localhost:5432/test")))),
                        new ConfigurationProperties(new Properties()),
                        TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")),
                Arguments.of("empty_storage_units_default_mysql", createDatabaseConfiguration(Collections.emptyMap()), new ConfigurationProperties(new Properties()),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")));
    }
    
    private static Stream<Arguments> getProtocolTypeWithDatabaseConfigurationsArguments() throws SQLException {
        Map<String, DatabaseConfiguration> multipleDatabaseConfigs = new LinkedHashMap<>(2, 1F);
        multipleDatabaseConfigs.put("foo_db", createDatabaseConfiguration(Collections.singletonMap("foo_ds", createDataSource(createConnectionWithUrl("jdbc:mysql://localhost:3306/test")))));
        multipleDatabaseConfigs.put("bar_db", createDatabaseConfiguration(Collections.singletonMap("bar_ds", createDataSource(createConnectionWithUrl("jdbc:postgresql://localhost:5432/test")))));
        return Stream.of(
                Arguments.of("configured_mysql", Collections.singletonMap("foo_db", createDatabaseConfiguration(Collections.emptyMap())),
                        createConfiguredProperties("MySQL"), TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("configured_h2_trunk_mysql", Collections.singletonMap("foo_db", createDatabaseConfiguration(Collections.emptyMap())),
                        createConfiguredProperties("H2"), TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("storage_unit_postgresql",
                        Collections.singletonMap("foo_db", createDatabaseConfiguration(
                                Collections.singletonMap("foo_ds", createDataSource(createConnectionWithUrl("jdbc:postgresql://localhost:5432/test"))))),
                        new ConfigurationProperties(new Properties()), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")),
                Arguments.of("empty_storage_units_default_mysql", Collections.singletonMap("foo_db", createDatabaseConfiguration(Collections.emptyMap())),
                        new ConfigurationProperties(new Properties()), TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("multiple_databases_use_first_storage_unit", multipleDatabaseConfigs, new ConfigurationProperties(new Properties()),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")));
    }
    
    private static Stream<Arguments> getStorageTypeArguments() throws SQLException {
        Connection firstConnection = createConnectionWithUnsupportedUrl();
        Connection secondConnection = mock(Connection.class);
        DialectJdbcUrlFetcher fetcher = createDialectJdbcUrlFetcher(secondConnection, "jdbc:postgresql://localhost:5432/test");
        return Stream.of(
                Arguments.of("direct_h2_url", createDataSource(createConnectionWithUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL")),
                        Collections.emptyList(), TypedSPILoader.getService(DatabaseType.class, "H2")),
                Arguments.of("direct_mysql_url", createDataSource(createConnectionWithUrl("jdbc:mysql://localhost:3306/test")),
                        Collections.emptyList(), TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("dialect_fetcher_postgresql_url", createDataSource(firstConnection, secondConnection),
                        Collections.singleton(fetcher), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
    }
    
    private static Stream<Arguments> getStorageTypeWithSQLWrapperExceptionArguments() throws SQLException {
        DialectJdbcUrlFetcher unmatchedFetcher = mock(DialectJdbcUrlFetcher.class);
        DialectJdbcUrlFetcher fetcher = mock(DialectJdbcUrlFetcher.class);
        Connection fourthConnection = mock(Connection.class);
        when(unmatchedFetcher.getConnectionClass()).thenAnswer(invocation -> Connection.class);
        when(fetcher.getConnectionClass()).thenAnswer(invocation -> Connection.class);
        when(fourthConnection.isWrapperFor(Connection.class)).thenThrow(new SQLException("wrapper error"));
        return Stream.of(
                Arguments.of("get_connection_error", createDataSourceWithConnectionException(), Collections.emptyList(), SQLException.class),
                Arguments.of("sql_feature_not_supported_with_unmatched_dialect_fetcher",
                        createDataSource(createConnectionWithUnsupportedUrl(), mock(Connection.class)),
                        Collections.singleton(unmatchedFetcher), SQLFeatureNotSupportedException.class),
                Arguments.of("sql_feature_not_supported_with_null_connection",
                        createDataSource(createConnectionWithUnsupportedUrl(), null), Collections.emptyList(), SQLFeatureNotSupportedException.class),
                Arguments.of("sql_feature_not_supported_with_wrapper_error",
                        createDataSource(createConnectionWithUnsupportedUrl(), fourthConnection), Collections.singleton(fetcher), SQLException.class));
    }
    
    private static DatabaseConfiguration createDatabaseConfiguration(final Map<String, DataSource> dataSources) {
        DatabaseConfiguration result = mock(DatabaseConfiguration.class);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class);
            when(storageUnit.getDataSource()).thenReturn(entry.getValue());
            storageUnits.put(entry.getKey(), storageUnit);
        }
        when(result.getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    private static ConfigurationProperties createConfiguredProperties(final String databaseType) {
        return new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), databaseType)));
    }
    
    private static DataSource createDataSource(final Connection... connections) throws SQLException {
        DataSource result = mock(DataSource.class);
        if (1 == connections.length) {
            when(result.getConnection()).thenReturn(connections[0]);
            return result;
        }
        Connection[] remainingConnections = Arrays.copyOfRange(connections, 1, connections.length);
        when(result.getConnection()).thenReturn(connections[0], remainingConnections);
        return result;
    }
    
    private static DataSource createDataSourceWithConnectionException() throws SQLException {
        DataSource result = mock(DataSource.class);
        when(result.getConnection()).thenThrow(SQLException.class);
        return result;
    }
    
    private static Connection createConnectionWithUrl(final String url) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn(url);
        return result;
    }
    
    private static Connection createConnectionWithUnsupportedUrl() throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenThrow(SQLFeatureNotSupportedException.class);
        return result;
    }
    
    private static DialectJdbcUrlFetcher createDialectJdbcUrlFetcher(final Connection connection, final String url) throws SQLException {
        DialectJdbcUrlFetcher result = mock(DialectJdbcUrlFetcher.class);
        when(result.getConnectionClass()).thenAnswer(invocation -> Connection.class);
        when(connection.isWrapperFor(Connection.class)).thenReturn(true);
        when(result.fetch(connection)).thenReturn(url);
        return result;
    }
}
