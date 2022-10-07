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

package org.apache.shardingsphere.infra.datasource.props;

import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourcePropertiesTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGetDataSourceConfigurationWithConnectionInitSqls() {
        MockedDataSource actualDataSource = new MockedDataSource();
        actualDataSource.setDriverClassName(MockedDataSource.class.getName());
        actualDataSource.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        DataSourceProperties actual = DataSourcePropertiesCreator.create(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getAllLocalProperties().get("url").toString(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getAllLocalProperties().get("username").toString(), is("root"));
        assertThat(actual.getAllLocalProperties().get("password").toString(), is("root"));
        assertNull(actual.getAllLocalProperties().get("loginTimeout"));
        assertThat(actual.getAllLocalProperties().get("connectionInitSqls"), instanceOf(List.class));
        List<String> actualConnectionInitSql = (List<String>) actual.getAllLocalProperties().get("connectionInitSqls");
        assertThat(actualConnectionInitSql, hasItem("set names utf8mb4;"));
        assertThat(actualConnectionInitSql, hasItem("set names utf8;"));
    }
    
    @Test
    public void assertGetAllLocalProperties() {
        DataSourceProperties originalDataSourceProps = new DataSourceProperties(MockedDataSource.class.getName(), getProperties());
        Map<String, Object> actualAllProps = originalDataSourceProps.getAllLocalProperties();
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
        Map<String, Object> result = new HashMap<>(7, 1);
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
    public void assertEquals() {
        assertThat(new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("root")),
                is(new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("root"))));
    }
    
    @Test
    public void assertNotEqualsWithNullValue() {
        assertFalse(new DataSourceProperties(MockedDataSource.class.getName(), new HashMap<>()).equals(null));
    }
    
    @Test
    public void assertNotEqualsWithDifferentDataSourceClassName() {
        assertThat(new DataSourceProperties("FooDataSourceClass", new HashMap<>()), not(new DataSourceProperties("BarDataSourceClass", new HashMap<>())));
    }
    
    @Test
    public void assertNotEqualsWithDifferentProperties() {
        DataSourceProperties actual = new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("foo"));
        DataSourceProperties expected = new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("bar"));
        assertThat(actual, not(expected));
    }
    
    @Test
    public void assertSameHashCode() {
        assertThat(new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("root")).hashCode(),
                is(new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("root")).hashCode()));
    }
    
    @Test
    public void assertDifferentHashCodeWithDifferentDataSourceClassName() {
        assertThat(new DataSourceProperties("FooDataSourceClass", createUserProperties("foo")).hashCode(),
                not(new DataSourceProperties("BarDataSourceClass", createUserProperties("foo")).hashCode()));
    }
    
    @Test
    public void assertDifferentHashCodeWithDifferentProperties() {
        assertThat(new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("foo")).hashCode(),
                not(new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("bar")).hashCode()));
    }
    
    private Map<String, Object> createUserProperties(final String username) {
        Map<String, Object> result = new LinkedHashMap<>(1, 1);
        result.put("username", username);
        return result;
    }
}
