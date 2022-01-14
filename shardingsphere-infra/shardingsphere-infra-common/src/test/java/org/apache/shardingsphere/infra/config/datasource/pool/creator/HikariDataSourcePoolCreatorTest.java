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

package org.apache.shardingsphere.infra.config.datasource.pool.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HikariDataSourcePoolCreatorTest {
    
    @Test
    public void assertCreate() {
        DataSource dataSource = DataSourcePoolCreator.create(createDataSourceProperties());
        assertThat(dataSource, instanceOf(HikariDataSource.class));
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getJdbcUrl(), is("jdbc:mysql://127.0.0.1/foo_ds"));
        assertThat(hikariDataSource.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertThat(hikariDataSource.getUsername(), is("root"));
        assertThat(hikariDataSource.getPassword(), is("root"));
        assertThat(hikariDataSource.getMaximumPoolSize(), is(10));
        assertThat(hikariDataSource.getMinimumIdle(), is(1));
        assertProperties(hikariDataSource.getDataSourceProperties());
    }
    
    private DataSourceProperties createDataSourceProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:mysql://127.0.0.1/foo_ds");
        props.put("driverClassName", MockedDataSource.class.getName());
        props.put("username", "root");
        props.put("password", "root");
        props.put("maxPoolSize", 10);
        props.put("minPoolSize", 1);
        props.put("dataSourceProperties", createJdbcUrlProperties());
        DataSourceProperties result = new DataSourceProperties(HikariDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
    
    private Properties createJdbcUrlProperties() {
        Properties result = new Properties();
        result.put("prepStmtCacheSqlLimit", 1024);
        result.put("cachePrepStmts", true);
        result.put("prepStmtCacheSize", 1000);
        return result;
    }
    
    private void assertProperties(final Properties actual) {
        assertThat(actual.get("prepStmtCacheSqlLimit"), is(1024));
        assertThat(actual.get("cachePrepStmts"), is(true));
        assertThat(actual.get("prepStmtCacheSize"), is(1000));
        assertThat(actual.get("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(actual.get("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(actual.get("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(actual.get("cacheResultSetMetadata"), is(Boolean.FALSE.toString()));
        assertThat(actual.get("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(actual.get("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(actual.get("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(actual.get("netTimeoutForStreamingResults"), is("0"));
        assertThat(actual.get("tinyInt1isBit"), is(Boolean.FALSE.toString()));
        assertThat(actual.get("useSSL"), is(Boolean.FALSE.toString()));
        assertThat(actual.get("serverTimezone"), is("UTC"));
    }
}
