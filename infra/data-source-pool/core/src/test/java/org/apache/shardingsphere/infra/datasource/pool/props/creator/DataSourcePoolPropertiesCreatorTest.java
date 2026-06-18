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

package org.apache.shardingsphere.infra.datasource.pool.props.creator;

import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolReflection;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DataSourcePoolPropertiesCreatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithDataSourceConfigurationArguments")
    void assertCreateWithDataSourceConfiguration(final String name, final DataSourceConfiguration config, final Map<String, Object> expectedProperties) {
        DataSourcePoolProperties actual = DataSourcePoolPropertiesCreator.create(config);
        assertThat(actual.getPoolClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getAllLocalProperties(), is(expectedProperties));
    }
    
    @Test
    void assertCreateWithDataSourceWithoutMetaData() {
        MockedDataSource dataSource = createMetaDataAwareDataSource();
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolMetaData.class, MockedDataSource.class.getName())).thenReturn(Optional.empty());
            DataSourcePoolProperties actual = DataSourcePoolPropertiesCreator.create(dataSource);
            assertThat(actual.getPoolClassName(), is(MockedDataSource.class.getName()));
            assertThat(actual.getAllLocalProperties(), is(createExpectedPropertiesWithoutMetaData()));
        }
    }
    
    private Map<String, Object> createExpectedPropertiesWithoutMetaData() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("username", "root");
        result.put("password", "root");
        result.put("maxPoolSize", -1);
        result.put("minPoolSize", 2);
        result.put("closed", false);
        result.put("openedConnections", Collections.emptySet());
        return result;
    }
    
    @Test
    void assertCreateWithCatalogSwitchableDataSource() {
        MockedDataSource dataSource = createMetaDataAwareDataSource();
        dataSource.close();
        DataSourcePoolProperties actual = DataSourcePoolPropertiesCreator.create(new CatalogSwitchableDataSource(dataSource, "catalog_db", "jdbc:mock://127.0.0.1/catalog_ds"));
        assertThat(actual.getPoolClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getAllLocalProperties(), is(createExpectedPropertiesWithMetaData()));
    }
    
    private MockedDataSource createMetaDataAwareDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName(MockedDataSource.class.getName());
        result.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        result.setUsername("root");
        result.setPassword("root");
        result.setMaxPoolSize(-1);
        result.setMinPoolSize(2);
        return result;
    }
    
    private Map<String, Object> createExpectedPropertiesWithMetaData() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        result.put("minPoolSize", 2);
        result.put("openedConnections", Collections.emptySet());
        return result;
    }
    
    @Test
    void assertCreateWithNullPropertyValue() {
        Map<String, Object> properties = new LinkedHashMap<>(5, 1F);
        properties.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        properties.put("driverClassName", MockedDataSource.class.getName());
        properties.put("username", "root");
        properties.put("password", "root");
        properties.put("maxPoolSize", null);
        try (
                MockedConstruction<DataSourcePoolReflection> ignored = mockConstruction(DataSourcePoolReflection.class, (mock, context) -> when(mock.convertToProperties()).thenReturn(properties))) {
            DataSourcePoolProperties actual = DataSourcePoolPropertiesCreator.create(new MockedDataSource());
            assertThat(actual.getPoolClassName(), is(MockedDataSource.class.getName()));
            assertThat(actual.getAllLocalProperties(), is(createExpectedPropertiesWithNullPropertyValue()));
        }
    }
    
    private Map<String, Object> createExpectedPropertiesWithNullPropertyValue() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("username", "root");
        result.put("password", "root");
        result.put("maxPoolSize", null);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createConfigurationArguments")
    void assertCreateConfiguration(final String name, final DataSourcePoolProperties props,
                                   final Long expectedConnectionTimeoutMilliseconds, final Long expectedIdleTimeoutMilliseconds, final Long expectedMaxLifetimeMilliseconds,
                                   final Integer expectedMaxPoolSize, final Integer expectedMinPoolSize, final Boolean expectedReadOnly, final Properties expectedCustomProperties) {
        DataSourceConfiguration actual = DataSourcePoolPropertiesCreator.createConfiguration(props);
        assertConnectionConfiguration(actual.getConnection());
        assertPoolConfiguration(actual.getPool(), expectedConnectionTimeoutMilliseconds, expectedIdleTimeoutMilliseconds, expectedMaxLifetimeMilliseconds,
                expectedMaxPoolSize, expectedMinPoolSize, expectedReadOnly, expectedCustomProperties);
    }
    
    private void assertPoolConfiguration(final PoolConfiguration actual, final Long expectedConnectionTimeoutMilliseconds,
                                         final Long expectedIdleTimeoutMilliseconds, final Long expectedMaxLifetimeMilliseconds,
                                         final Integer expectedMaxPoolSize, final Integer expectedMinPoolSize,
                                         final Boolean expectedReadOnly, final Properties expectedCustomProperties) {
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(expectedConnectionTimeoutMilliseconds));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(expectedIdleTimeoutMilliseconds));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(expectedMaxLifetimeMilliseconds));
        assertThat(actual.getMaxPoolSize(), is(expectedMaxPoolSize));
        assertThat(actual.getMinPoolSize(), is(expectedMinPoolSize));
        assertThat(actual.getReadOnly(), is(expectedReadOnly));
        assertThat(actual.getCustomProperties(), is(expectedCustomProperties));
        assertNull(actual.getCustomProperties().getProperty("username"));
    }
    
    private void assertConnectionConfiguration(final ConnectionConfiguration actual) {
        assertThat(actual.getDataSourceClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    private static Stream<Arguments> createWithDataSourceConfigurationArguments() {
        Properties customProperties = new Properties();
        customProperties.setProperty("jdbcUrlProperties.socketTimeout", "30");
        Properties overridingCustomProperties = new Properties();
        overridingCustomProperties.setProperty("url", "jdbc:mock://127.0.0.1/override_ds");
        return Stream.of(
                Arguments.of("without custom props", createDataSourceConfiguration(null), createExpectedPropertiesWithDataSourceConfiguration("jdbc:mock://127.0.0.1/foo_ds", null)),
                Arguments.of("with custom props", createDataSourceConfiguration(customProperties),
                        createExpectedPropertiesWithDataSourceConfiguration("jdbc:mock://127.0.0.1/foo_ds", PropertiesBuilder.build(new Property("socketTimeout", "30")))),
                Arguments.of("with overriding custom props", createDataSourceConfiguration(overridingCustomProperties),
                        createExpectedPropertiesWithDataSourceConfiguration("jdbc:mock://127.0.0.1/override_ds", null)));
    }
    
    private static DataSourceConfiguration createDataSourceConfiguration(final Properties customProperties) {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(MockedDataSource.class.getName(), MockedDataSource.class.getName(),
                "jdbc:mock://127.0.0.1/foo_ds", "root", "root");
        PoolConfiguration poolConfig = new PoolConfiguration(1000L, 2000L, 3000L, 8, 2, false, customProperties);
        return new DataSourceConfiguration(connectionConfig, poolConfig);
    }
    
    private static Map<String, Object> createExpectedPropertiesWithDataSourceConfiguration(final String url, final Properties jdbcUrlProperties) {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("dataSourceClassName", MockedDataSource.class.getName());
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("url", url);
        result.put("username", "root");
        result.put("password", "root");
        result.put("connectionTimeoutMilliseconds", 1000L);
        result.put("idleTimeoutMilliseconds", 2000L);
        result.put("maxLifetimeMilliseconds", 3000L);
        result.put("maxPoolSize", 8);
        result.put("minPoolSize", 2);
        result.put("readOnly", false);
        if (null != jdbcUrlProperties) {
            result.put("jdbcUrlProperties", jdbcUrlProperties);
        }
        return result;
    }
    
    private static Stream<Arguments> createConfigurationArguments() {
        return Stream.of(
                Arguments.of("valid values", new DataSourcePoolProperties(MockedDataSource.class.getName(), createValidProperties()), 120000L, 180000L, 240000L, 30, 10, false,
                        PropertiesBuilder.build(new Property("jdbcUrlProperties", PropertiesBuilder.build(new Property("socketTimeout", "30"))))),
                Arguments.of("missing optional values", new DataSourcePoolProperties(MockedDataSource.class.getName(), createBaseProperties()), null, null, null, null, null, null, new Properties()),
                Arguments.of("invalid numeric values", new DataSourcePoolProperties(MockedDataSource.class.getName(), createInvalidNumericProperties()), null, null, null, null, null, true,
                        new Properties()));
    }
    
    private static Map<String, Object> createBaseProperties() {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("dataSourceClassName", MockedDataSource.class.getName());
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
    
    private static Map<String, Object> createValidProperties() {
        Map<String, Object> result = createBaseProperties();
        result.put("connectionTimeoutMilliseconds", "120000");
        result.put("idleTimeoutMilliseconds", 180000L);
        result.put("maxLifetimeMilliseconds", "240000");
        result.put("maxPoolSize", "30");
        result.put("minPoolSize", 10);
        result.put("readOnly", false);
        result.put("jdbcUrlProperties.socketTimeout", "30");
        return result;
    }
    
    private static Map<String, Object> createInvalidNumericProperties() {
        Map<String, Object> result = createBaseProperties();
        result.put("connectionTimeoutMilliseconds", "invalid");
        result.put("idleTimeoutMilliseconds", "invalid");
        result.put("maxLifetimeMilliseconds", "invalid");
        result.put("maxPoolSize", "invalid");
        result.put("minPoolSize", "invalid");
        result.put("readOnly", "true");
        return result;
    }
}
