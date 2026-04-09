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

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectDefaultQueryPropertiesProvider;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolFieldMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DataSourcePoolReflectionTest {
    
    @Test
    void assertConvertToProperties() {
        DataSourcePoolReflectionFixture dataSource = new DataSourcePoolReflectionFixture();
        Properties jdbcUrlProperties = PropertiesBuilder.build(new Property("socketTimeout", "30"));
        dataSource.setStringValue("foo_value");
        dataSource.setEnabled(true);
        dataSource.setListValue(Collections.singletonList("init_sql"));
        dataSource.setJdbcUrlProperties(jdbcUrlProperties);
        Map<String, Object> actual = new DataSourcePoolReflection(dataSource).convertToProperties();
        assertThat(actual.size(), is(4));
        assertThat(actual.get("stringValue"), is("foo_value"));
        assertTrue((Boolean) actual.get("enabled"));
        assertThat(actual.get("listValue"), is(Collections.singletonList("init_sql")));
        assertThat(actual.get("jdbcUrlProperties"), is(jdbcUrlProperties));
        assertFalse(actual.containsKey("loginTimeout"));
        assertFalse(actual.containsKey("nullValue"));
        assertFalse(actual.containsKey("objectValue"));
        assertFalse(actual.containsKey("ignoredValue"));
    }
    
    @Test
    void assertConvertToPropertiesWithGetterFailure() {
        DataSourcePoolReflectionFixture dataSource = new DataSourcePoolReflectionFixture();
        dataSource.setThrowOnGetStringValue(true);
        assertThrows(InvocationTargetException.class, () -> new DataSourcePoolReflection(dataSource).convertToProperties());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("setFieldWithConvertedValues")
    void assertSetFieldWithConvertedValues(final String name, final String fieldName, final Object fieldValue, final String actualFieldName, final Object expectedFieldValue) {
        DataSourcePoolReflectionFixture dataSource = new DataSourcePoolReflectionFixture();
        new DataSourcePoolReflection(dataSource).setField(fieldName, fieldValue);
        Object actualFieldValue = getFieldValue(dataSource, actualFieldName);
        assertThat(actualFieldValue, is(expectedFieldValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("setFieldWithIgnoredValues")
    void assertSetFieldWithIgnoredValues(final String name, final String fieldName, final Object fieldValue, final String actualFieldName, final Object expectedFieldValue) {
        DataSourcePoolReflectionFixture dataSource = new DataSourcePoolReflectionFixture();
        new DataSourcePoolReflection(dataSource).setField(fieldName, fieldValue);
        Object actualFieldValue = getFieldValue(dataSource, actualFieldName);
        assertThat(actualFieldValue, is(expectedFieldValue));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object getFieldValue(final DataSourcePoolReflectionFixture dataSource, final String fieldName) {
        return Plugins.getMemberAccessor().get(DataSourcePoolReflectionFixture.class.getDeclaredField(fieldName), dataSource);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("addDefaultDataSourcePoolPropertiesWithEarlyReturn")
    void assertAddDefaultDataSourcePoolPropertiesWithEarlyReturn(final String name, final boolean hasSpiMetaData,
                                                                 final String jdbcUrl, final Properties jdbcUrlProperties,
                                                                 final Properties expectedJdbcUrlProperties) {
        DataSourcePoolReflectionFixture dataSource = new DataSourcePoolReflectionFixture();
        DataSourcePoolMetaData metaData = mockDataSourcePoolMetaData();
        dataSource.setUrl(jdbcUrl);
        dataSource.setJdbcUrlProperties(jdbcUrlProperties);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolMetaData.class, DataSourcePoolReflectionFixture.class.getName()))
                    .thenReturn(hasSpiMetaData ? Optional.of(metaData) : Optional.empty());
            new DataSourcePoolReflection(dataSource).addDefaultDataSourcePoolProperties(metaData);
        }
        Properties actualJdbcUrlProperties = dataSource.getJdbcUrlProperties();
        if (null == expectedJdbcUrlProperties) {
            assertNull(actualJdbcUrlProperties);
        } else {
            assertThat(actualJdbcUrlProperties, is(expectedJdbcUrlProperties));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("addDefaultDataSourcePoolPropertiesWithDefaultQueryProperties")
    void assertAddDefaultDataSourcePoolPropertiesWithDefaultQueryProperties(final String name, final Properties jdbcUrlProperties,
                                                                            final Properties queryProperties, final Properties defaultQueryProps, final Properties expectedJdbcUrlProps) {
        DataSourcePoolReflectionFixture dataSource = new DataSourcePoolReflectionFixture();
        DataSourcePoolMetaData metaData = mockDataSourcePoolMetaData();
        DatabaseType databaseType = mock(DatabaseType.class);
        ConnectionProperties connectionProperties = mock(ConnectionProperties.class);
        dataSource.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        dataSource.setJdbcUrlProperties(jdbcUrlProperties);
        when(connectionProperties.getQueryProperties()).thenReturn(queryProperties);
        ConnectionPropertiesParser connectionPropertiesParser = mock(ConnectionPropertiesParser.class);
        when(connectionPropertiesParser.parse("jdbc:mock://127.0.0.1/foo_ds", null, null)).thenReturn(connectionProperties);
        DialectDefaultQueryPropertiesProvider defaultQueryPropsProvider = mock(DialectDefaultQueryPropertiesProvider.class);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class);
                MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolMetaData.class, DataSourcePoolReflectionFixture.class.getName())).thenReturn(Optional.of(metaData));
            databaseTypeFactory.when(() -> DatabaseTypeFactory.get("jdbc:mock://127.0.0.1/foo_ds")).thenReturn(databaseType);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(connectionPropertiesParser);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDefaultQueryPropertiesProvider.class, databaseType))
                    .thenReturn(null == defaultQueryProps ? Optional.empty() : Optional.of(defaultQueryPropsProvider));
            if (null != defaultQueryProps) {
                when(defaultQueryPropsProvider.getDefaultQueryProperties()).thenReturn(defaultQueryProps);
            }
            new DataSourcePoolReflection(dataSource).addDefaultDataSourcePoolProperties(metaData);
        }
        assertThat(dataSource.getJdbcUrlProperties(), is(expectedJdbcUrlProps));
    }
    
    private DataSourcePoolMetaData mockDataSourcePoolMetaData() {
        DataSourcePoolFieldMetaData fieldMetaData = mock(DataSourcePoolFieldMetaData.class);
        when(fieldMetaData.getJdbcUrlFieldName()).thenReturn("url");
        when(fieldMetaData.getJdbcUrlPropertiesFieldName()).thenReturn("jdbcUrlProperties");
        DataSourcePoolMetaData result = mock(DataSourcePoolMetaData.class);
        when(result.getFieldMetaData()).thenReturn(fieldMetaData);
        return result;
    }
    
    private static Stream<Arguments> setFieldWithConvertedValues() {
        return Stream.of(
                Arguments.of("integer value converted", "integerValue", "7", "integerValue", 7),
                Arguments.of("primitive integer value converted", "primitiveIntegerValue", "6", "integerValue", 6),
                Arguments.of("long value converted", "longValue", "8", "longValue", 8L),
                Arguments.of("primitive long value converted", "primitiveLongValue", "9", "longValue", 9L),
                Arguments.of("boolean value converted", "enabled", "true", "enabled", true),
                Arguments.of("primitive boolean value converted", "primitiveEnabled", "true", "enabled", true),
                Arguments.of("string value converted", "stringValue", 123, "stringValue", "123"),
                Arguments.of("properties value converted", "jdbcUrlProperties",
                        PropertiesBuilder.build(new Property("socketTimeout", "30")), "jdbcUrlProperties", PropertiesBuilder.build(new Property("socketTimeout", "30"))),
                Arguments.of("duration value converted", "connectionTimeout", "5", "connectionTimeout", Duration.ofSeconds(5L)),
                Arguments.of("custom value assigned directly", "customValue", Collections.singletonList("assigned"), "customValue", Collections.singletonList("assigned")));
    }
    
    private static Stream<Arguments> setFieldWithIgnoredValues() {
        return Stream.of(
                Arguments.of("null value ignored", "stringValue", null, "stringValue", "initial_string"),
                Arguments.of("skipped property ignored", "loginTimeout", 9, "loginTimeout", 0),
                Arguments.of("matched setter with wrong parameter count ignored", "brokenValue", "ignored", "stringValue", "initial_string"),
                Arguments.of("missing setter ignored", "missingValue", "ignored", "stringValue", "initial_string"),
                Arguments.of("string field ignores properties value", "stringValue", PropertiesBuilder.build(new Property("url", "jdbc:mock://127.0.0.1/foo_ds")), "stringValue", "initial_string"));
    }
    
    private static Stream<Arguments> addDefaultDataSourcePoolPropertiesWithEarlyReturn() {
        return Stream.of(
                Arguments.of("missing SPI metadata returns early", false, "jdbc:mock://127.0.0.1/foo_ds",
                        PropertiesBuilder.build(new Property("existing", "value")), PropertiesBuilder.build(new Property("existing", "value"))),
                Arguments.of("missing JDBC URL returns early", true, null,
                        PropertiesBuilder.build(new Property("existing", "value")), PropertiesBuilder.build(new Property("existing", "value"))),
                Arguments.of("missing JDBC properties returns early", true, "jdbc:mock://127.0.0.1/foo_ds", null, null));
    }
    
    private static Stream<Arguments> addDefaultDataSourcePoolPropertiesWithDefaultQueryProperties() {
        return Stream.of(
                Arguments.of("missing provider keeps original properties",
                        PropertiesBuilder.build(new Property("existing", "value")),
                        new Properties(),
                        null,
                        PropertiesBuilder.build(new Property("existing", "value"))),
                Arguments.of("missing default property gets appended",
                        PropertiesBuilder.build(new Property("existing", "value")),
                        new Properties(),
                        PropertiesBuilder.build(new Property("socketTimeout", "30")),
                        PropertiesBuilder.build(new Property("existing", "value"), new Property("socketTimeout", "30"))),
                Arguments.of("existing JDBC property blocks default",
                        PropertiesBuilder.build(new Property("socketTimeout", "60")),
                        new Properties(),
                        PropertiesBuilder.build(new Property("socketTimeout", "30")),
                        PropertiesBuilder.build(new Property("socketTimeout", "60"))),
                Arguments.of("existing query property blocks default",
                        PropertiesBuilder.build(new Property("existing", "value")),
                        PropertiesBuilder.build(new Property("socketTimeout", "45")),
                        PropertiesBuilder.build(new Property("socketTimeout", "30")),
                        PropertiesBuilder.build(new Property("existing", "value"))));
    }
}
