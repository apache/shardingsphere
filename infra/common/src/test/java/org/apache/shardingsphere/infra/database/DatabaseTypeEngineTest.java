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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    
    @Test
    void assertGetStorageType() throws SQLException {
        DatabaseType expectedDatabaseType = mock(DatabaseType.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(databaseMetaData)).thenReturn(expectedDatabaseType);
            assertThat(DatabaseTypeEngine.getStorageType(createDataSource(createConnectionWithMetadata(databaseMetaData))), is(expectedDatabaseType));
        }
    }
    
    @Test
    void assertGetStorageTypeWithConnectionException() throws SQLException {
        SQLException expectedCause = new SQLException("connection failed");
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(expectedCause);
        SQLWrapperException actual = assertThrows(SQLWrapperException.class, () -> DatabaseTypeEngine.getStorageType(dataSource));
        assertThat(actual.getCause(), is(expectedCause));
    }
    
    @Test
    void assertGetStorageTypeWithMetadataException() throws SQLException {
        SQLException expectedCause = new SQLException("metadata failed");
        Connection connection = mock(Connection.class);
        when(connection.getMetaData()).thenThrow(expectedCause);
        SQLWrapperException actual = assertThrows(SQLWrapperException.class, () -> DatabaseTypeEngine.getStorageType(createDataSource(connection)));
        assertThat(actual.getCause(), is(expectedCause));
    }
    
    @Test
    void assertGetStorageTypeWithDatabaseTypeFactorySQLException() {
        SQLException expectedCause = new SQLException("factory failed");
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(databaseMetaData)).thenThrow(expectedCause);
            SQLWrapperException actual = assertThrows(SQLWrapperException.class,
                    () -> DatabaseTypeEngine.getStorageType(createDataSource(createConnectionWithMetadata(databaseMetaData))));
            assertThat(actual.getCause(), is(expectedCause));
        }
    }
    
    @Test
    void assertGetStorageTypeWithUnsupportedStorageType() {
        UnsupportedStorageTypeException expectedException = new UnsupportedStorageTypeException("jdbc:unsupported:test");
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(databaseMetaData)).thenThrow(expectedException);
            UnsupportedStorageTypeException actual = assertThrows(UnsupportedStorageTypeException.class,
                    () -> DatabaseTypeEngine.getStorageType(createDataSource(createConnectionWithMetadata(databaseMetaData))));
            assertThat(actual, is(expectedException));
        }
    }
    
    @Test
    void assertGetStorageTypeWithRuntimeException() {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            mocked.when(() -> DatabaseTypeFactory.get(databaseMetaData)).thenThrow(new IllegalStateException("boom"));
            IllegalStateException actual = assertThrows(IllegalStateException.class,
                    () -> DatabaseTypeEngine.getStorageType(createDataSource(createConnectionWithMetadata(databaseMetaData))));
            assertThat(actual.getMessage(), is("boom"));
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
    
    private static DataSource createDataSource(final Connection connection) throws SQLException {
        DataSource result = mock(DataSource.class);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    private static Connection createConnectionWithUrl(final String url) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn(url);
        return result;
    }
    
    private static Connection createConnectionWithMetadata(final DatabaseMetaData databaseMetaData) throws SQLException {
        Connection result = mock(Connection.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        return result;
    }
}
