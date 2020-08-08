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
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class DataSourceConfigurationTest {
    
    @Test
    public void assertGetDataSourceConfiguration() throws SQLException {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName("org.h2.Driver");
        actualDataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setLoginTimeout(1);
        DataSourceConfiguration actual = DataSourceConfiguration.getDataSourceConfiguration(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProps().get("jdbcUrl").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertNull(actual.getProps().get("loginTimeout"));
    }
    
    @Test
    public void assertCreateDataSource() {
        Map<String, Object> props = new HashMap<>();
        props.put("driverClassName", "org.h2.Driver");
        props.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("username", "root");
        props.put("password", "root");
        props.put("loginTimeout", "5000");
        props.put("test", "test");
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfig.getProps().putAll(props);
        HikariDataSource actual = (HikariDataSource) dataSourceConfig.createDataSource();
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    public void assertAddAlias() {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName("org.h2.Driver");
        actualDataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        DataSourceConfiguration actual = DataSourceConfiguration.getDataSourceConfiguration(actualDataSource);
        actual.addPropertyAlias("url", "jdbcUrl");
        actual.addPropertyAlias("user", "username");
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProps().get("jdbcUrl").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertThat(actual.getProps().get("jdbcUrl"), is(actual.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(actual.getProps().get("user")));
    }
    
    @Test
    public void assertEquals() {
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration(HikariDataSource.class.getName());
        DataSourceConfiguration targetDataSourceConfiguration = new DataSourceConfiguration(HikariDataSource.class.getName());
        assertThat(originalDataSourceConfig.equals(originalDataSourceConfig), is(true));
        assertThat(originalDataSourceConfig.equals(targetDataSourceConfiguration), is(true));
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfiguration.getProps().put("username", "root");
        assertThat(originalDataSourceConfig.equals(targetDataSourceConfiguration), is(true));
        targetDataSourceConfiguration.getProps().put("password", "root");
        assertThat(originalDataSourceConfig.equals(targetDataSourceConfiguration), is(true));
        originalDataSourceConfig.getProps().put("username", "root");
        originalDataSourceConfig.getProps().put("url", "jdbcUrl");
        assertThat(originalDataSourceConfig.equals(targetDataSourceConfiguration), is(false));
    }
    
    @Test
    public void assertHashCode() {
        DataSourceConfiguration originalDataSourceConfig = new DataSourceConfiguration(HikariDataSource.class.getName());
        DataSourceConfiguration targetDataSourceConfig = new DataSourceConfiguration(HikariDataSource.class.getName());
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().put("username", "root");
        originalDataSourceConfig.getProps().put("password", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("password", "root");
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().put("url", "jdbcUrl");
        assertThat(originalDataSourceConfig.hashCode(), not(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().clear();
        targetDataSourceConfig = new DataSourceConfiguration(BasicDataSource.class.getName());
        assertThat(originalDataSourceConfig.hashCode(), not(targetDataSourceConfig.hashCode()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGetDataSourceConfigurationWithConnectionInitSqls() {
        BasicDataSource actualDataSource = new BasicDataSource();
        actualDataSource.setDriverClassName("org.h2.Driver");
        actualDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        DataSourceConfiguration actual = DataSourceConfiguration.getDataSourceConfiguration(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(BasicDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProps().get("url").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertNull(actual.getProps().get("loginTimeout"));
        assertThat(actual.getProps().get("connectionInitSqls"), instanceOf(List.class));
        List<String> actualConnectionInitSql = (List<String>) actual.getProps().get("connectionInitSqls");
        assertThat(actualConnectionInitSql, hasItem("set names utf8mb4;"));
        assertThat(actualConnectionInitSql, hasItem("set names utf8;"));
    }
}
