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

import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourcePoolPropertiesTest {
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetDataSourceConfigurationWithConnectionInitSqls() {
        MockedDataSource actualDataSource = new MockedDataSource();
        actualDataSource.setDriverClassName(MockedDataSource.class.getName());
        actualDataSource.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setConnectionInitSqls(Arrays.asList("SET names utf8mb4;", "SET names utf8;"));
        DataSourcePoolProperties actual = DataSourcePoolPropertiesCreator.create(actualDataSource);
        assertThat(actual.getPoolClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getAllLocalProperties().get("driverClassName").toString(), is(MockedDataSource.class.getName()));
        assertThat(actual.getAllLocalProperties().get("url").toString(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getAllLocalProperties().get("username").toString(), is("root"));
        assertThat(actual.getAllLocalProperties().get("password").toString(), is("root"));
        assertNull(actual.getAllLocalProperties().get("loginTimeout"));
        assertThat(actual.getAllLocalProperties().get("connectionInitSqls"), isA(List.class));
        List<String> actualConnectionInitSql = (List<String>) actual.getAllLocalProperties().get("connectionInitSqls");
        assertThat(actualConnectionInitSql, hasItem("SET names utf8mb4;"));
        assertThat(actualConnectionInitSql, hasItem("SET names utf8;"));
    }
    
    @Test
    void assertGetAllLocalProperties() {
        DataSourcePoolProperties originalProps = new DataSourcePoolProperties(MockedDataSource.class.getName(), getProperties());
        Map<String, Object> actualAllProps = originalProps.getAllLocalProperties();
        assertThat(actualAllProps.size(), is(7));
        assertTrue(actualAllProps.containsKey("driverClassName"));
        assertTrue(actualAllProps.containsValue(MockedDataSource.class.getName()));
        assertTrue(actualAllProps.containsKey("jdbcUrl"));
        assertTrue(actualAllProps.containsValue("jdbc:mock://127.0.0.1/foo_ds"));
        assertTrue(actualAllProps.containsKey("username"));
        assertTrue(actualAllProps.containsValue("root"));
        assertTrue(actualAllProps.containsKey("password"));
        assertTrue(actualAllProps.containsValue("root"));
        assertTrue(actualAllProps.containsKey("loginTimeout"));
        assertTrue(actualAllProps.containsValue("5000"));
        assertTrue(actualAllProps.containsKey("maximumPoolSize"));
        assertTrue(actualAllProps.containsValue("30"));
        assertTrue(actualAllProps.containsKey("idleTimeout"));
        assertTrue(actualAllProps.containsValue("30000"));
    }
    
    private Map<String, Object> getProperties() {
        Map<String, Object> result = new HashMap<>(7, 1F);
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        result.put("loginTimeout", "5000");
        result.put("maximumPoolSize", "30");
        result.put("idleTimeout", "30000");
        return result;
    }
    
    @Test
    void assertEquals() {
        assertThat(new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("root")),
                is(new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("root"))));
    }
    
    @Test
    void assertNotEqualsWithNullValue() {
        assertThat(new DataSourcePoolProperties(MockedDataSource.class.getName(), Collections.emptyMap()), not(nullValue()));
    }
    
    @Test
    void assertNotEqualsWithDifferentDataSourceClassName() {
        assertThat(new DataSourcePoolProperties("FooDataSourceClass", Collections.emptyMap()), not(new DataSourcePoolProperties("BarDataSourceClass", Collections.emptyMap())));
    }
    
    @Test
    void assertNotEqualsWithDifferentProperties() {
        DataSourcePoolProperties actual = new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("foo"));
        DataSourcePoolProperties expected = new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("bar"));
        assertThat(actual, not(expected));
    }
    
    @Test
    void assertSameHashCode() {
        assertThat(new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("root")).hashCode(),
                is(new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("root")).hashCode()));
    }
    
    @Test
    void assertDifferentHashCodeWithDifferentDataSourceClassName() {
        assertThat(new DataSourcePoolProperties("FooDataSourceClass", createUserProperties("foo")).hashCode(),
                not(new DataSourcePoolProperties("BarDataSourceClass", createUserProperties("foo")).hashCode()));
    }
    
    @Test
    void assertDifferentHashCodeWithDifferentProperties() {
        assertThat(new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("foo")).hashCode(),
                not(new DataSourcePoolProperties(MockedDataSource.class.getName(), createUserProperties("bar")).hashCode()));
    }
    
    private Map<String, Object> createUserProperties(final String username) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("username", username);
        result.put("dataSourceProperties", createDataSourcePoolProperties());
        return result;
    }
    
    private Map<String, String> createDataSourcePoolProperties() {
        Map<String, String> result = new LinkedHashMap<>(3, 1F);
        result.put("maintainTimeStats", Boolean.FALSE.toString());
        result.put("rewriteBatchedStatements", Boolean.TRUE.toString());
        result.put("useLocalSessionState", Boolean.TRUE.toString());
        return result;
    }
}
