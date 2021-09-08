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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
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

public final class DataSourceConverterTest {
    
    @Test
    public void assertGetDataSourceMap() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = new HashMap<>(1, 1);
        dataSourceConfigurationMap.put("ds_0", createDataSourceConfiguration());
        Map<String, DataSource> actual = DataSourceConverter.getDataSourceMap(dataSourceConfigurationMap);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertGetDataSourceConfigurationMap() {
        Map<String, DataSourceConfiguration> actual = DataSourceConverter.getDataSourceConfigurationMap(createDataSourceMap());
        assertThat(actual.size(), is(2));
        assertNotNull(actual.get("ds_0"));
        assertNotNull(actual.get("ds_1"));
    }
    
    @Test
    public void assertGetDataSourceConfiguration() throws SQLException {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName("org.h2.Driver");
        actualDataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setLoginTimeout(1);
        DataSourceConfiguration actual = DataSourceConverter.getDataSourceConfiguration(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProps().get("jdbcUrl").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertNull(actual.getProps().get("loginTimeout"));
    }
    
    @Test
    public void assertGetDataSource() {
        HikariDataSource actual = (HikariDataSource) DataSourceConverter.getDataSource(createDataSourceConfiguration());
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getMaximumPoolSize(), is(50));
        assertThat(actual.getMinimumIdle(), is(1));
        assertThat(actual.getMaxLifetime(), is(60000L));
    }
    
    @Test
    public void assertCreateDataSourceWithIntegerPassword() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", "org.h2.Driver");
        props.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("username", "root");
        props.put("password", 123);
        props.put("loginTimeout", "5000");
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfig.getProps().putAll(props);
        HikariDataSource actual = (HikariDataSource) DataSourceConverter.getDataSource(dataSourceConfig);
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
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
    
    private DataSourceConfiguration createDataSourceConfiguration() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", "org.h2.Driver");
        props.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("username", "root");
        props.put("password", "root");
        props.put("loginTimeout", "5000");
        props.put("maximumPoolSize", "50");
        props.put("minimumIdle", "1");
        props.put("maxLifetime", "60000");
        props.put("test", "test");
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
}
