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

package org.apache.shardingsphere.infra.datasource.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.creator.DataSourceCreator;
import org.apache.shardingsphere.infra.config.datasource.creator.impl.HikariDataSourceCreator;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HikariDataSourceCreatorTest {
    
    @Test
    public void assertCreateDataSourceConfiguration() {
        DataSourceCreator dataSourceCreator = new HikariDataSourceCreator();
        DataSourceConfiguration configuration = dataSourceCreator.createDataSourceConfiguration(dataSourceCreator.createDataSource(createDataSourceConfiguration()));
        assertThat(configuration.getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(configuration.getProps().get("jdbcUrl"), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(configuration.getProps().get("driverClassName"), is("org.h2.Driver"));
        assertThat(configuration.getProps().get("username"), is("root"));
        assertThat(configuration.getProps().get("password"), is("root"));
        assertThat(configuration.getProps().get("maximumPoolSize"), is(10));
        assertThat(configuration.getProps().get("minimumIdle"), is(1));
    }
    
    @Test
    public void assertCreateDataSource() {
        DataSourceCreator dataSourceCreator = new HikariDataSourceCreator();
        DataSource dataSource = dataSourceCreator.createDataSource(createDataSourceConfiguration());
        assertThat(dataSource, instanceOf(HikariDataSource.class));
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(hikariDataSource.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(hikariDataSource.getDriverClassName(), is("org.h2.Driver"));
        assertThat(hikariDataSource.getUsername(), is("root"));
        assertThat(hikariDataSource.getPassword(), is("root"));
        assertThat(hikariDataSource.getMaximumPoolSize(), is(10));
        assertThat(hikariDataSource.getMinimumIdle(), is(1));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        props.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("driverClassName", "org.h2.Driver");
        props.put("username", "root");
        props.put("password", "root");
        props.put("maxPoolSize", 10);
        props.put("minPoolSize", 1);
        DataSourceConfiguration result = new DataSourceConfiguration(String.valueOf(props.get("dataSourceClassName")));
        result.getProps().putAll(props);
        return result;
    }
}
