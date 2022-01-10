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

package org.apache.shardingsphere.infra.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
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

public final class DataSourceConfigurationTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void assertAddSynonym() {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName(MockedDataSource.class.getCanonicalName());
        actualDataSource.setJdbcUrl("jdbc:mock://127.0.0.1/foo_ds");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        DataSourceConfiguration actual = DataSourcePoolCreatorUtil.getDataSourceConfiguration(actualDataSource);
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
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        DataSourceConfiguration targetDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        assertThat(originalDataSourceConfig, is(originalDataSourceConfig));
        assertThat(originalDataSourceConfig, is(targetDataSourceConfig));
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        assertThat(originalDataSourceConfig, is(targetDataSourceConfig));
        targetDataSourceConfig.getProps().put("password", "root");
        assertThat(originalDataSourceConfig, is(targetDataSourceConfig));
    }
    
    @Test
    public void assertNotEqualsWithNullValue() {
        assertFalse(new DataSourceConfiguration("FooDataSourceClass").equals(null));
    }
    
    @Test
    public void assertNotEqualsWithDifferentDataSourceClassName() {
        assertThat(new DataSourceConfiguration("FooDataSourceClass"), not(new DataSourceConfiguration("BarDataSourceClass")));
    }
    
    @Test
    public void assertNotEqualsWithDifferentProperties() {
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        DataSourceConfiguration targetDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root0");
        assertThat(originalDataSourceConfig, not(targetDataSourceConfig));
    }
    
    @Test
    public void assertSameHashCode() {
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        DataSourceConfiguration targetDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().put("password", "root");
        targetDataSourceConfig.getProps().put("password", "root");
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
    }
    
    @Test
    public void assertDifferentHashCode() {
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        DataSourceConfiguration targetDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("password", "root");
        assertThat(originalDataSourceConfig.hashCode(), not(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        targetDataSourceConfig = new DataSourceConfiguration("BarDataSourceClass");
        assertThat(originalDataSourceConfig.hashCode(), not(targetDataSourceConfig.hashCode()));
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
        DataSourceConfiguration actual = DataSourcePoolCreatorUtil.getDataSourceConfiguration(actualDataSource);
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertCreateDataSourceWithCustomPoolProps() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", MockedDataSource.class.getCanonicalName());
        props.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("username", "root");
        props.put("password", "root");
        props.put("loginTimeout", "5000");
        Properties customPoolProps = new Properties();
        customPoolProps.setProperty("maximumPoolSize", "30");
        customPoolProps.setProperty("idleTimeout", "30000");
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfig.getProps().putAll(props);
        dataSourceConfig.getProps().putAll(new HashMap(customPoolProps));
        HikariDataSource actual = (HikariDataSource) DataSourcePoolCreatorUtil.getDataSource(dataSourceConfig);
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getCanonicalName()));
        assertThat(actual.getJdbcUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getMaximumPoolSize(), is(30));
        assertThat(actual.getIdleTimeout(), is(30000L));
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
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration("FooDataSourceClass");
        originalDataSourceConfig.getProps().putAll(props);
        originalDataSourceConfig.getProps().putAll(new HashMap(customPoolProps));
        Map<String, Object> actualAllProperties = originalDataSourceConfig.getAllProperties();
        assertNotNull(actualAllProperties);
        assertThat(actualAllProperties.size(), is(7));
        assertTrue(actualAllProperties.containsKey("driverClassName"));
        assertTrue(actualAllProperties.containsValue(MockedDataSource.class.getCanonicalName()));
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
