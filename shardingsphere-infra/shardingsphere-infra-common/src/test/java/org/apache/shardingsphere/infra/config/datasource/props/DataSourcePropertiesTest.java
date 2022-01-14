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

package org.apache.shardingsphere.infra.config.datasource.props;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreatorUtil;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourcePropertiesTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void assertAddSynonym() {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName(MockedDataSource.class.getCanonicalName());
        actualDataSource.setJdbcUrl("jdbc:mock://127.0.0.1/foo_ds");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        DataSourceProperties actual = DataSourcePoolCreatorUtil.getDataSourceProperties(actualDataSource);
        actual.addPropertySynonym("url", "jdbcUrl");
        actual.addPropertySynonym("user", "username");
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is(MockedDataSource.class.getCanonicalName()));
        assertThat(actual.getProps().get("jdbcUrl").toString(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertThat(actual.getProps().get("jdbcUrl"), is(actual.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(actual.getProps().get("user")));
    }
    
    @Test
    public void assertEquals() {
        DataSourceProperties originalDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        DataSourceProperties targetDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        assertThat(originalDataSourceProps, is(originalDataSourceProps));
        assertThat(originalDataSourceProps, is(targetDataSourceProps));
        originalDataSourceProps.getProps().put("username", "root");
        targetDataSourceProps.getProps().put("username", "root");
        assertThat(originalDataSourceProps, is(targetDataSourceProps));
        targetDataSourceProps.getProps().put("password", "root");
        assertThat(originalDataSourceProps, is(targetDataSourceProps));
    }
    
    @Test
    public void assertNotEqualsWithNullValue() {
        assertFalse(new DataSourceProperties("FooDataSourceClass").equals(null));
    }
    
    @Test
    public void assertNotEqualsWithDifferentDataSourceClassName() {
        assertThat(new DataSourceProperties("FooDataSourceClass"), not(new DataSourceProperties("BarDataSourceClass")));
    }
    
    @Test
    public void assertNotEqualsWithDifferentProperties() {
        DataSourceProperties originalDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        DataSourceProperties targetDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        originalDataSourceProps.getProps().put("username", "root");
        targetDataSourceProps.getProps().put("username", "root0");
        assertThat(originalDataSourceProps, not(targetDataSourceProps));
    }
    
    @Test
    public void assertSameHashCode() {
        DataSourceProperties originalDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        DataSourceProperties targetDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        assertThat(originalDataSourceProps.hashCode(), is(targetDataSourceProps.hashCode()));
        originalDataSourceProps.getProps().put("username", "root");
        targetDataSourceProps.getProps().put("username", "root");
        assertThat(originalDataSourceProps.hashCode(), is(targetDataSourceProps.hashCode()));
        originalDataSourceProps.getProps().put("password", "root");
        targetDataSourceProps.getProps().put("password", "root");
        assertThat(originalDataSourceProps.hashCode(), is(targetDataSourceProps.hashCode()));
    }
    
    @Test
    public void assertDifferentHashCode() {
        DataSourceProperties originalDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        DataSourceProperties targetDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        originalDataSourceProps.getProps().put("username", "root");
        targetDataSourceProps.getProps().put("username", "root");
        targetDataSourceProps.getProps().put("password", "root");
        assertThat(originalDataSourceProps.hashCode(), not(targetDataSourceProps.hashCode()));
        originalDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        targetDataSourceProps = new DataSourceProperties("BarDataSourceClass");
        assertThat(originalDataSourceProps.hashCode(), not(targetDataSourceProps.hashCode()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGetDataSourceConfigurationWithConnectionInitSqls() {
        BasicDataSource actualDataSource = new BasicDataSource();
        actualDataSource.setDriverClassName(MockedDataSource.class.getCanonicalName());
        actualDataSource.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        DataSourceProperties actual = DataSourcePoolCreatorUtil.getDataSourceProperties(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(BasicDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is(MockedDataSource.class.getCanonicalName()));
        assertThat(actual.getProps().get("url").toString(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertNull(actual.getProps().get("loginTimeout"));
        assertThat(actual.getProps().get("connectionInitSqls"), instanceOf(List.class));
        List<String> actualConnectionInitSql = (List<String>) actual.getProps().get("connectionInitSqls");
        assertThat(actualConnectionInitSql, hasItem("set names utf8mb4;"));
        assertThat(actualConnectionInitSql, hasItem("set names utf8;"));
    }
    
    @Test
    public void assertGetAllProperties() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", MockedDataSource.class.getCanonicalName());
        props.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("username", "root");
        props.put("password", "root");
        props.put("loginTimeout", "5000");
        Properties customPoolProps = new Properties();
        customPoolProps.setProperty("maximumPoolSize", "30");
        customPoolProps.setProperty("idleTimeout", "30000");
        DataSourceProperties originalDataSourceProps = new DataSourceProperties("FooDataSourceClass");
        originalDataSourceProps.getProps().putAll(props);
        originalDataSourceProps.getProps().putAll(new HashMap(customPoolProps));
        Map<String, Object> actualAllProperties = originalDataSourceProps.getAllProperties();
        assertNotNull(actualAllProperties);
        assertThat(actualAllProperties.size(), is(7));
        assertTrue(actualAllProperties.containsKey("driverClassName"));
        assertTrue(actualAllProperties.containsValue(MockedDataSource.class.getName()));
        assertTrue(actualAllProperties.containsKey("jdbcUrl"));
        assertTrue(actualAllProperties.containsValue("jdbc:mock://127.0.0.1/foo_ds"));
        assertTrue(actualAllProperties.containsKey("username"));
        assertTrue(actualAllProperties.containsValue("root"));
        assertTrue(actualAllProperties.containsKey("password"));
        assertTrue(actualAllProperties.containsValue("root"));
        assertTrue(actualAllProperties.containsKey("loginTimeout"));
        assertTrue(actualAllProperties.containsValue("5000"));
        assertTrue(actualAllProperties.containsKey("maximumPoolSize"));
        assertTrue(actualAllProperties.containsValue("30"));
        assertTrue(actualAllProperties.containsKey("idleTimeout"));
        assertTrue(actualAllProperties.containsValue("30000"));
    }
}
