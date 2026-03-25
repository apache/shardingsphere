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

package org.apache.shardingsphere.infra.datasource.pool.creator;

import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectDefaultQueryPropertiesProvider;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolFieldMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DataSourcePoolCreatorTest {
    
    @BeforeEach
    @AfterEach
    void clearCachedDataSources() {
        GlobalDataSourceRegistry.getInstance().getCachedDataSources().clear();
    }
    
    @Test
    void assertCreateMap() {
        Map<String, DataSource> actual = DataSourcePoolCreator.create(
                Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties())), true);
        assertThat(actual.size(), is(1));
        assertMockedDataSource((MockedDataSource) actual.get("foo_ds"));
    }
    
    @Test
    void assertCreateMapWithEmptyProperties() {
        assertTrue(DataSourcePoolCreator.create(Collections.emptyMap(), true).isEmpty());
    }
    
    @Test
    void assertCreateWithMetaData() {
        assertMockedDataSource((MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties())));
    }
    
    @Test
    void assertCreateWithInvalidPoolClassName() {
        assertThrows(ClassNotFoundException.class, () -> DataSourcePoolCreator.create(new DataSourcePoolProperties("missing.DataSource", Collections.emptyMap())));
    }
    
    @Test
    void assertCreateWithoutMetaData() {
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolMetaData.class, MockedDataSource.class.getName())).thenReturn(Optional.empty());
            MockedDataSource actual = (MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties()));
            assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
            assertThat(actual.getUsername(), is("root"));
            assertThat(actual.getPassword(), is("root"));
            assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
            assertThat(actual.getConnectionTimeout(), is(Duration.ofSeconds(120)));
            assertNull(actual.getMaxPoolSize());
            assertNull(actual.getMinPoolSize());
            assertNull(actual.getJdbcUrlProperties());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithMinPoolSizeSettings")
    void assertCreateWithMinPoolSizeSettings(final String name, final Integer minPoolSize, final Integer expectedMinPoolSize) {
        Map<String, Object> props = createMockedDataSourceProperties();
        props.put("minPoolSize", minPoolSize);
        MockedDataSource actual = (MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), props));
        assertThat(actual.getMaxPoolSize(), is(100));
        assertThat(actual.getMinPoolSize(), is(expectedMinPoolSize));
    }
    
    @Test
    void assertCreateWithJdbcUrlProperties() {
        DataSourcePoolMetaData metaData = mockDataSourcePoolMetaData(MockedDataSource.class.getName(), "jdbcUrlProperties", true);
        DatabaseType databaseType = mock(DatabaseType.class);
        ConnectionProperties connectionProperties = mock(ConnectionProperties.class);
        ConnectionPropertiesParser connectionPropertiesParser = mock(ConnectionPropertiesParser.class);
        Properties queryProperties = new Properties();
        when(connectionProperties.getQueryProperties()).thenReturn(queryProperties);
        when(connectionPropertiesParser.parse("jdbc:mock://127.0.0.1/foo_ds", null, null)).thenReturn(connectionProperties);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class);
                MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolMetaData.class, MockedDataSource.class.getName())).thenReturn(Optional.of(metaData));
            databaseTypeFactory.when(() -> DatabaseTypeFactory.get("jdbc:mock://127.0.0.1/foo_ds")).thenReturn(databaseType);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(connectionPropertiesParser);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDefaultQueryPropertiesProvider.class, databaseType)).thenReturn(Optional.empty());
            Map<String, Object> props = createMockedDataSourceProperties();
            props.put("jdbcUrlProperties.socketTimeout", 30);
            MockedDataSource actual = (MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), props));
            assertMockedDataSource(actual);
            assertThat(actual.getMaxPoolSize(), is(100));
            assertThat(actual.getJdbcUrlProperties().getProperty("socketTimeout"), is("30"));
        }
    }
    
    @Test
    void assertCreateWithNullJdbcUrlPropertiesFieldName() {
        DataSourcePoolMetaData metaData = mockDataSourcePoolMetaData(MockedDataSource.class.getName(), null, false);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolMetaData.class, MockedDataSource.class.getName())).thenReturn(Optional.of(metaData));
            MockedDataSource actual = (MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties()));
            assertMockedDataSource(actual);
            assertNull(actual.getJdbcUrlProperties());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithCacheSettings")
    void assertCreateWithCacheSettings(final String name, final boolean cacheEnabled, final boolean preloaded,
                                       final boolean expectedCached, final boolean expectedReturnedDataSourceCached, final int expectedSize) {
        Map<String, DataSource> cachedDataSources = GlobalDataSourceRegistry.getInstance().getCachedDataSources();
        if (preloaded) {
            cachedDataSources.put("foo_ds", new MockedDataSource());
        }
        DataSource actual = DataSourcePoolCreator.create("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties()), cacheEnabled);
        assertThat(cachedDataSources.containsKey("foo_ds"), is(expectedCached));
        assertThat(cachedDataSources.get("foo_ds") == actual, is(expectedReturnedDataSourceCached));
        assertThat(cachedDataSources.size(), is(expectedSize));
        assertMockedDataSource((MockedDataSource) actual);
    }
    
    @Test
    void assertCreateWithStorageNodes() {
        MockedDataSource storageNode = mock(MockedDataSource.class);
        MockedDataSource actual = (MockedDataSource) DataSourcePoolCreator.create(
                "foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties()), false, Collections.singleton(storageNode));
        assertMockedDataSource(actual);
        assertThrows(org.mockito.exceptions.verification.WantedButNotInvoked.class, () -> org.mockito.Mockito.verify(storageNode).close());
    }
    
    @Test
    void assertCreateWithStorageNodesAndCacheDisabledOnFailure() throws SQLException {
        MockedDataSource storageNode = mock(MockedDataSource.class);
        doReturn(storageNode).when(storageNode).unwrap(MockedDataSource.class);
        doReturn(Collections.emptySet()).when(storageNode).getOpenedConnections();
        Map<String, Object> props = createMockedDataSourceProperties();
        props.put("connectionTimeout", "invalid");
        assertThrows(NumberFormatException.class,
                () -> DataSourcePoolCreator.create("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), props), false, Collections.singleton(storageNode)));
        Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollInterval(10L, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> org.mockito.Mockito.verify(storageNode, org.mockito.Mockito.atLeastOnce()).close());
    }
    
    @Test
    void assertCreateWithStorageNodesAndCacheEnabledOnFailure() {
        MockedDataSource storageNode = mock(MockedDataSource.class);
        Map<String, Object> props = createMockedDataSourceProperties();
        props.put("connectionTimeout", "invalid");
        assertThrows(NumberFormatException.class,
                () -> DataSourcePoolCreator.create("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), props), true, Collections.singleton(storageNode)));
        Awaitility.await().during(200L, TimeUnit.MILLISECONDS).atMost(500L, TimeUnit.MILLISECONDS).untilAsserted(() -> org.mockito.Mockito.verify(storageNode, org.mockito.Mockito.never()).close());
    }
    
    private Map<String, Object> createMockedDataSourceProperties() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("connectionTimeout", "120");
        return result;
    }
    
    private DataSourcePoolMetaData mockDataSourcePoolMetaData(final String type, final String jdbcUrlPropertiesFieldName, final boolean withDefaultJdbcUrlProperties) {
        DataSourcePoolFieldMetaData fieldMetaData = mock(DataSourcePoolFieldMetaData.class);
        when(fieldMetaData.getJdbcUrlFieldName()).thenReturn("url");
        when(fieldMetaData.getJdbcUrlPropertiesFieldName()).thenReturn(jdbcUrlPropertiesFieldName);
        DataSourcePoolMetaData result = mock(DataSourcePoolMetaData.class);
        Map<String, Object> defaultProperties = new LinkedHashMap<>(2, 1F);
        defaultProperties.put("maxPoolSize", 100);
        if (withDefaultJdbcUrlProperties) {
            defaultProperties.put("jdbcUrlProperties", new Properties());
        }
        when(result.getDefaultProperties()).thenReturn(defaultProperties);
        Map<String, Object> skippedProperties = new LinkedHashMap<>(2, 1F);
        skippedProperties.put("maxPoolSize", -1);
        skippedProperties.put("minPoolSize", -1);
        when(result.getSkippedProperties()).thenReturn(skippedProperties);
        when(result.getPropertySynonyms()).thenReturn(Collections.emptyMap());
        when(result.getTransientFieldNames()).thenReturn(Collections.emptyList());
        when(result.getFieldMetaData()).thenReturn(fieldMetaData);
        when(result.getType()).thenReturn(type);
        return result;
    }
    
    private void assertMockedDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getMaxPoolSize(), is(100));
        assertNull(actual.getMinPoolSize());
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getConnectionTimeout(), is(Duration.ofSeconds(120)));
    }
    
    private static Stream<Arguments> createWithMinPoolSizeSettings() {
        return Stream.of(
                Arguments.of("skipped property keeps null", -1, null),
                Arguments.of("null property keeps null", null, null),
                Arguments.of("configured property overrides default", 10, 10));
    }
    
    private static Stream<Arguments> createWithCacheSettings() {
        return Stream.of(
                Arguments.of("cache disabled", false, false, false, false, 0),
                Arguments.of("cache enabled without existing cache", true, false, true, true, 1),
                Arguments.of("cache enabled with existing cache", true, true, true, false, 1));
    }
}
