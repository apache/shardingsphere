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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    
    @Test
    void assertGetProtocolTypeWithDetectedBranchStorageType() {
        DatabaseType trunkDatabaseType = mock(DatabaseType.class);
        DatabaseType branchDatabaseType = mock(DatabaseType.class);
        when(branchDatabaseType.getTrunkDatabaseType()).thenReturn(Optional.of(trunkDatabaseType));
        assertThat(DatabaseTypeEngine.getProtocolType(branchDatabaseType), is(trunkDatabaseType));
    }
    
    @Test
    void assertGetProtocolTypeWithStorageTypeWithoutBranchDetection() {
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        assertThat(DatabaseTypeEngine.getProtocolType(databaseType), is(databaseType));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getStorageTypeArguments")
    void assertGetStorageType(final String name, final DataSource dataSource, final Collection<DialectJdbcUrlFetcher> fetchers, final DatabaseType expectedDatabaseType) {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(fetchers);
            assertThat(DatabaseTypeEngine.getStorageType(dataSource), is(expectedDatabaseType));
        }
    }
    
    @Test
    void assertGetStorageTypeWithURLAndBranchDetectionOption() throws SQLException {
        String url = "jdbc:trunk://localhost:3306/test";
        Connection connection = mock(Connection.class);
        DataSource dataSource = createDataSource(connection);
        DatabaseType urlDatabaseType = mock(DatabaseType.class);
        DatabaseType actualDatabaseType = mock(DatabaseType.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(url)).thenReturn(urlDatabaseType);
            mocked.when(() -> DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(urlDatabaseType)).thenReturn(true);
            mocked.when(() -> DatabaseTypeFactory.getActualDatabaseType(urlDatabaseType, connection)).thenReturn(actualDatabaseType);
            assertThat(DatabaseTypeEngine.getStorageType(url, dataSource), is(actualDatabaseType));
        }
    }
    
    @Test
    void assertGetStorageTypeWithURLAndNoBranchDetectionOption() throws SQLException {
        String url = "jdbc:trunk://localhost:3306/test";
        DataSource dataSource = mock(DataSource.class);
        DatabaseType urlDatabaseType = mock(DatabaseType.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(url)).thenReturn(urlDatabaseType);
            mocked.when(() -> DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(urlDatabaseType)).thenReturn(false);
            assertThat(DatabaseTypeEngine.getStorageType(url, dataSource), is(urlDatabaseType));
        }
        verify(dataSource, never()).getConnection();
    }
    
    @Test
    void assertGetStorageTypeWithDataSourcePoolPropertiesAndBranchDetectionOption() throws SQLException {
        String url = "jdbc:trunk://localhost:3306/test";
        Connection connection = mock(Connection.class);
        Driver driver = createDriver(url, connection);
        DatabaseType urlDatabaseType = mock(DatabaseType.class);
        DatabaseType actualDatabaseType = mock(DatabaseType.class);
        DataSourcePoolProperties dataSourcePoolProps = createDataSourcePoolProperties(url);
        DriverManager.registerDriver(driver);
        try (MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class)) {
            databaseTypeFactory.when(() -> DatabaseTypeFactory.get(url)).thenReturn(urlDatabaseType);
            databaseTypeFactory.when(() -> DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(urlDatabaseType)).thenReturn(true);
            databaseTypeFactory.when(() -> DatabaseTypeFactory.getActualDatabaseType(urlDatabaseType, connection)).thenReturn(actualDatabaseType);
            assertThat(DatabaseTypeEngine.getStorageType(dataSourcePoolProps), is(actualDatabaseType));
        } finally {
            DriverManager.deregisterDriver(driver);
        }
        ArgumentCaptor<Properties> actualProps = ArgumentCaptor.forClass(Properties.class);
        verify(driver).connect(eq(url), actualProps.capture());
        assertThat(actualProps.getValue().getProperty("user"), is("root"));
        assertThat(actualProps.getValue().getProperty("password"), is("root"));
        assertThat(actualProps.getValue().getProperty("socketTimeout"), is("30"));
        verify(connection).close();
    }
    
    @Test
    void assertGetStorageTypeWithDataSourcePoolPropertiesAndNoBranchDetectionOption() throws SQLException {
        String url = "jdbc:trunk://localhost:3306/test";
        Driver driver = mock(Driver.class);
        DataSourcePoolProperties dataSourcePoolProps = createDataSourcePoolProperties(url);
        DatabaseType urlDatabaseType = mock(DatabaseType.class);
        DriverManager.registerDriver(driver);
        try (MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class)) {
            databaseTypeFactory.when(() -> DatabaseTypeFactory.get(url)).thenReturn(urlDatabaseType);
            databaseTypeFactory.when(() -> DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(urlDatabaseType)).thenReturn(false);
            assertThat(DatabaseTypeEngine.getStorageType(dataSourcePoolProps), is(urlDatabaseType));
            verifyNoInteractions(driver);
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }
    
    @Test
    void assertGetStorageTypeWithDataSourcePoolPropertiesAndDriverClassName() {
        String url = "jdbc:driver-class-required://localhost:3306/test";
        DatabaseType urlDatabaseType = mock(DatabaseType.class);
        DatabaseType actualDatabaseType = mock(DatabaseType.class);
        DataSourcePoolProperties dataSourcePoolProps = createDataSourcePoolProperties(url, DriverClassNameRequiredDriver.class.getName());
        try (MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class)) {
            databaseTypeFactory.when(() -> DatabaseTypeFactory.get(url)).thenReturn(urlDatabaseType);
            databaseTypeFactory.when(() -> DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(urlDatabaseType)).thenReturn(true);
            databaseTypeFactory.when(() -> DatabaseTypeFactory.getActualDatabaseType(eq(urlDatabaseType), any(Connection.class))).thenReturn(actualDatabaseType);
            assertThat(DatabaseTypeEngine.getStorageType(dataSourcePoolProps), is(actualDatabaseType));
        }
    }
    
    @Test
    void assertGetStorageTypeWithURLAndConnectionFailure() throws SQLException {
        String url = "jdbc:trunk://localhost:3306/test";
        DataSource dataSource = createDataSourceWithConnectionException();
        DatabaseType urlDatabaseType = mock(DatabaseType.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(url)).thenReturn(urlDatabaseType);
            mocked.when(() -> DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(urlDatabaseType)).thenReturn(true);
            assertThat(assertThrows(SQLWrapperException.class, () -> DatabaseTypeEngine.getStorageType(url, dataSource)).getCause(), isA(SQLException.class));
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
    
    private static Stream<Arguments> getProtocolTypeWithDatabaseConfigurationArguments() {
        return Stream.of(
                Arguments.of("configured_mysql", createDatabaseConfiguration(Collections.emptyMap()), createConfiguredProperties("MySQL"),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("configured_h2_trunk_mysql", createDatabaseConfiguration(Collections.emptyMap()), createConfiguredProperties("H2"),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("storage_unit_postgresql",
                        createDatabaseConfiguration(Collections.singletonMap("foo_ds", TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))),
                        new ConfigurationProperties(new Properties()),
                        TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")),
                Arguments.of("empty_storage_units_default_mysql", createDatabaseConfiguration(Collections.emptyMap()), new ConfigurationProperties(new Properties()),
                        TypedSPILoader.getService(DatabaseType.class, "MySQL")));
    }
    
    private static Stream<Arguments> getProtocolTypeWithDatabaseConfigurationsArguments() {
        Map<String, DatabaseConfiguration> multipleDatabaseConfigs = new LinkedHashMap<>(2, 1F);
        multipleDatabaseConfigs.put("foo_db", createDatabaseConfiguration(Collections.singletonMap("foo_ds", TypedSPILoader.getService(DatabaseType.class, "MySQL"))));
        multipleDatabaseConfigs.put("bar_db", createDatabaseConfiguration(Collections.singletonMap("bar_ds", TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))));
        return Stream.of(
                Arguments.of("configured_mysql", Collections.singletonMap("foo_db", createDatabaseConfiguration(Collections.emptyMap())),
                        createConfiguredProperties("MySQL"), TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("configured_h2_trunk_mysql", Collections.singletonMap("foo_db", createDatabaseConfiguration(Collections.emptyMap())),
                        createConfiguredProperties("H2"), TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Arguments.of("storage_unit_postgresql",
                        Collections.singletonMap("foo_db", createDatabaseConfiguration(
                                Collections.singletonMap("foo_ds", TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")))),
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
    
    private static DatabaseConfiguration createDatabaseConfiguration(final Map<String, DatabaseType> storageTypes) {
        DatabaseConfiguration result = mock(DatabaseConfiguration.class);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(storageTypes.size(), 1F);
        for (Entry<String, DatabaseType> entry : storageTypes.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class);
            when(storageUnit.getStorageType()).thenReturn(entry.getValue());
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
    
    private static DataSourcePoolProperties createDataSourcePoolProperties(final String url) {
        return createDataSourcePoolProperties(url, null);
    }
    
    private static DataSourcePoolProperties createDataSourcePoolProperties(final String url, final String driverClassName) {
        Map<String, Object> result = new LinkedHashMap<>(null == driverClassName ? 4 : 5, 1F);
        result.put("url", url);
        result.put("username", "root");
        result.put("password", "root");
        if (null != driverClassName) {
            result.put("driverClassName", driverClassName);
        }
        result.put("dataSourceProperties.socketTimeout", "30");
        return new DataSourcePoolProperties(MockedDataSource.class.getName(), result);
    }
    
    private static Driver createDriver(final String url, final Connection connection) throws SQLException {
        Driver result = mock(Driver.class);
        when(result.connect(eq(url), any(Properties.class))).thenReturn(connection);
        return result;
    }
    
    private static Connection createConnectionWithUrl(final String url) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getMetaData().getDatabaseProductName()).thenReturn("");
        when(result.getMetaData().getURL()).thenReturn(url);
        when(result.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT @@version_comment")).thenReturn(resultSet);
        when(statement.executeQuery("SELECT VERSION()")).thenReturn(resultSet);
        return result;
    }
    
    private static Connection createConnectionWithUnsupportedUrl() throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getDatabaseProductName()).thenReturn("");
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
    
    private static final class DriverClassNameRequiredDriver implements Driver {
        
        private static final Driver INSTANCE = new DriverClassNameRequiredDriver();
        
        static {
            try {
                DriverManager.registerDriver(INSTANCE);
            } catch (final SQLException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        @Override
        public Connection connect(final String url, final Properties info) {
            return acceptsURL(url) ? mock(Connection.class) : null;
        }
        
        @Override
        public boolean acceptsURL(final String url) {
            return url.startsWith("jdbc:driver-class-required:");
        }
        
        @Override
        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
            return new DriverPropertyInfo[0];
        }
        
        @Override
        public int getMajorVersion() {
            return 0;
        }
        
        @Override
        public int getMinorVersion() {
            return 0;
        }
        
        @Override
        public boolean jdbcCompliant() {
            return true;
        }
        
        @Override
        public Logger getParentLogger() {
            return mock(Logger.class);
        }
    }
}
