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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.creator.impl.DefaultDataSourceCreator;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultDataSourceCreatorTest {
    
    @Test
    public void assertDataSourceConfigurationEquals() {
        DefaultDataSourceCreator defaultDataSourceCreator = new DefaultDataSourceCreator();
        DataSourceConfiguration generateDataSourceConfiguration = defaultDataSourceCreator.createDataSourceConfiguration(createDataSource());
        DataSourceConfiguration targetDataSourceConfiguration = createDataSourceConfiguration();
        assertThat(generateDataSourceConfiguration, is(targetDataSourceConfiguration));
    }
    
    @Test
    public void assertCreateDataSource() {
        DefaultDataSourceCreator defaultDataSourceCreator = new DefaultDataSourceCreator();
        DataSource generateDataSource = defaultDataSourceCreator.createDataSource(createDataSourceConfiguration());
        assertThat(generateDataSource, instanceOf(HikariDataSource.class));
        HikariDataSource targetDataSource = (HikariDataSource) generateDataSource;
        assertThat(targetDataSource.getUsername(), is("root"));
        assertThat(targetDataSource.getPassword(), is("root"));
        assertThat(targetDataSource.getDriverClassName(), is("org.h2.Driver"));
        assertThat(targetDataSource.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
    }
    
    private DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }
    
    private DataSourceConfiguration createDataSourceConfiguration() {
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration.getProps().put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSourceConfiguration.getProps().put("driverClassName", "org.h2.Driver");
        dataSourceConfiguration.getProps().put("username", "root");
        dataSourceConfiguration.getProps().put("password", "root");
        return dataSourceConfiguration;
    }
}
