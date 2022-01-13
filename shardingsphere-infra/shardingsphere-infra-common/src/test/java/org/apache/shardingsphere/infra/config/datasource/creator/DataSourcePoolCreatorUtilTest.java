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

package org.apache.shardingsphere.infra.config.datasource.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceProperties;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class DataSourcePoolCreatorUtilTest {
    
    @Test
    public void assertGetDataSourceMap() {
        Map<String, DataSourceProperties> dataSourcePropsMap = new HashMap<>(1, 1);
        dataSourcePropsMap.put("ds_0", createDataSourceProperties());
        Map<String, DataSource> actual = DataSourcePoolCreatorUtil.getDataSourceMap(dataSourcePropsMap);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertGetDataSourceConfigurationMap() {
        Map<String, DataSourceProperties> actual = DataSourcePoolCreatorUtil.getDataSourcePropertiesMap(createDataSourceMap());
        assertThat(actual.size(), is(2));
        assertNotNull(actual.get("ds_0"));
        assertNotNull(actual.get("ds_1"));
    }
    
    @Test
    public void assertGetDataSourceConfiguration() throws SQLException {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName(MockedDataSource.class.getName());
        actualDataSource.setJdbcUrl("jdbc:mock://127.0.0.1/foo_ds");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setLoginTimeout(1);
        DataSourceProperties actual = DataSourcePoolCreatorUtil.getDataSourceConfiguration(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProperties().get("driverClassName").toString(), is(MockedDataSource.class.getCanonicalName()));
        assertThat(actual.getProperties().get("jdbcUrl").toString(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getProperties().get("username").toString(), is("root"));
        assertThat(actual.getProperties().get("password").toString(), is("root"));
        assertNull(actual.getProperties().get("loginTimeout"));
    }
    
    @Test
    public void assertGetDataSource() {
        HikariDataSource actual = (HikariDataSource) DataSourcePoolCreatorUtil.getDataSource(createDataSourceProperties());
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getCanonicalName()));
        assertThat(actual.getJdbcUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getMaximumPoolSize(), is(50));
        assertThat(actual.getMinimumIdle(), is(1));
        assertThat(actual.getMaxLifetime(), is(60000L));
    }
    
    @Test
    public void assertCreateDataSourceWithIntegerPassword() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", MockedDataSource.class.getCanonicalName());
        props.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("username", "root");
        props.put("password", 123);
        props.put("loginTimeout", "5000");
        DataSourceProperties dataSourceProps = new DataSourceProperties(HikariDataSource.class.getName());
        dataSourceProps.getProperties().putAll(props);
        HikariDataSource actual = (HikariDataSource) DataSourcePoolCreatorUtil.getDataSource(dataSourceProps);
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getCanonicalName()));
        assertThat(actual.getJdbcUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("123"));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private DataSourceProperties createDataSourceProperties() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", MockedDataSource.class.getCanonicalName());
        props.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("username", "root");
        props.put("password", "root");
        props.put("loginTimeout", "5000");
        props.put("maximumPoolSize", "50");
        props.put("minimumIdle", "1");
        props.put("maxLifetime", "60000");
        props.put("test", "test");
        DataSourceProperties result = new DataSourceProperties(HikariDataSource.class.getName());
        result.getProperties().putAll(props);
        return result;
    }
}
