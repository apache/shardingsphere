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

package org.apache.shardingsphere.infra.datasource.pool.props.domain;

import com.google.common.base.Objects;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DataSourcePoolPropertiesTest {
    
    private static final String CUSTOM_POOL_CLASS_NAME = "com.example.CustomDataSource";
    
    @Test
    void assertGetAllStandardProperties() {
        DataSourcePoolProperties dataSourcePoolProps = new DataSourcePoolProperties(CUSTOM_POOL_CLASS_NAME, createPropertiesWithoutMetaData());
        Map<String, Object> actual = dataSourcePoolProps.getAllStandardProperties();
        assertThat(dataSourcePoolProps.getPoolClassName(), is(CUSTOM_POOL_CLASS_NAME));
        assertThat(actual.size(), is(7));
        assertThat(actual.get("dataSourceClassName"), is(MockedDataSource.class.getName()));
        assertThat(actual.get("url"), is("jdbc:mock://127.0.0.1/demo_ds"));
        assertThat(actual.get("username"), is("root"));
        assertThat(actual.get("password"), is("secret"));
        assertThat(actual.get("connectionTimeoutMilliseconds"), is(5000L));
        assertThat(actual.get("dataSourceProperties"), is(PropertiesBuilder.build(new Property("rewriteBatchedStatements", Boolean.TRUE.toString()))));
        assertThat(actual.get("applicationName"), is("demo"));
    }
    
    private Map<String, Object> createPropertiesWithoutMetaData() {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("data_source_class_name", MockedDataSource.class.getName());
        result.put("url", "jdbc:mock://127.0.0.1/demo_ds");
        result.put("username", "root");
        result.put("password", "secret");
        result.put("connection_timeout_milliseconds", 5000L);
        result.put("dataSourceProperties.rewriteBatchedStatements", Boolean.TRUE);
        result.put("applicationName", "demo");
        return result;
    }
    
    @Test
    void assertGetAllLocalProperties() {
        DataSourcePoolProperties dataSourcePoolProps = new DataSourcePoolProperties(MockedDataSource.class.getName(), createPropertiesWithMetaData());
        Map<String, Object> actual = dataSourcePoolProps.getAllLocalProperties();
        assertThat(dataSourcePoolProps.getPoolClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.size(), is(7));
        assertThat(actual.get("driverClassName"), is(MockedDataSource.class.getName()));
        assertThat(actual.get("jdbcUrl"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.get("username"), is("root"));
        assertThat(actual.get("password"), is("secret"));
        assertThat(actual.get("maxPoolSize"), is(16));
        assertThat(actual.get("featureFlag"), is("enabled"));
        assertThat(actual.get("dataSourceProperties"), is(PropertiesBuilder.build(new Property("useLocalSessionState", Boolean.TRUE.toString()))));
        assertFalse(actual.containsKey("closed"));
    }
    
    private Map<String, Object> createPropertiesWithMetaData() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "secret");
        result.put("max_pool_size", 16);
        result.put("closed", Boolean.FALSE);
        result.put("featureFlag", "enabled");
        result.put("dataSourceProperties.useLocalSessionState", Boolean.TRUE);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("equalsArguments")
    void assertEquals(final String name, final DataSourcePoolProperties actual, final Object other, final boolean expected) {
        assertThat(actual.equals(other), is(expected));
    }
    
    @Test
    void assertHashCode() {
        DataSourcePoolProperties actual = new DataSourcePoolProperties(CUSTOM_POOL_CLASS_NAME, Collections.singletonMap("username", "root"));
        assertThat(actual.hashCode(), is(Objects.hashCode(CUSTOM_POOL_CLASS_NAME, "usernameroot")));
    }
    
    private static Stream<Arguments> equalsArguments() {
        DataSourcePoolProperties sameReference = createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false");
        return Stream.of(
                Arguments.of("same instance", sameReference, sameReference, true),
                Arguments.of("null", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"), null, false),
                Arguments.of("different type", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"), "value", false),
                Arguments.of("different pool class name", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"),
                        createComparableProperties("com.example.OtherDataSource", "root", "false"), false),
                Arguments.of("empty local properties", new DataSourcePoolProperties(CUSTOM_POOL_CLASS_NAME, Collections.emptyMap()),
                        new DataSourcePoolProperties(CUSTOM_POOL_CLASS_NAME, Collections.emptyMap()), true),
                Arguments.of("missing local property", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"),
                        createComparableProperties(CUSTOM_POOL_CLASS_NAME, null, "false"), true),
                Arguments.of("different map property", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"),
                        createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "true"), false),
                Arguments.of("different string property", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"),
                        createComparableProperties(CUSTOM_POOL_CLASS_NAME, "admin", "false"), false),
                Arguments.of("equivalent properties", createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"),
                        createComparableProperties(CUSTOM_POOL_CLASS_NAME, "root", "false"), true));
    }
    
    private static DataSourcePoolProperties createComparableProperties(final String poolClassName, final String username, final String rewriteBatchedStatements) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        if (null != username) {
            result.put("username", username);
        }
        result.put("dataSourceProperties.rewriteBatchedStatements", rewriteBatchedStatements);
        return new DataSourcePoolProperties(poolClassName, result);
    }
}
