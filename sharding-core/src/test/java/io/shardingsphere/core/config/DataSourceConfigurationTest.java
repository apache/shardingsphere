/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.config;

import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.constant.PoolType;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
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
        assertThat(actual.getProperties().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProperties().get("jdbcUrl").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProperties().get("username").toString(), is("root"));
        assertThat(actual.getProperties().get("password").toString(), is("root"));
        assertNull(actual.getProperties().get("loginTimeout"));
    }
    
    @Test
    public void assertCreateDataSource() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("driverClassName", "org.h2.Driver");
        properties.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        properties.put("username", "root");
        properties.put("password", "root");
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration(PoolType.HIKARI.getClassName());
        dataSourceConfig.getProperties().putAll(properties);
        HikariDataSource actual = (HikariDataSource) dataSourceConfig.createDataSource();
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
}
