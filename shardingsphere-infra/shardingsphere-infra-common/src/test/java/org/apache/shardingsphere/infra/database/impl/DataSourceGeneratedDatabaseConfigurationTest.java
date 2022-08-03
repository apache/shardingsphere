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

package org.apache.shardingsphere.infra.database.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.junit.Test;

public final class DataSourceGeneratedDatabaseConfigurationTest {
    
    @Test
    public void assertDataSourceGeneratedDatabaseConfiguration() {
        String url = "jdbc:mysql://192.168.0.1:3306/foo_ds";
        String username = "root";
        String password = "root";
        ConnectionConfiguration configuration = new ConnectionConfiguration(url, username, password);
        PoolConfiguration poolConfiguration = new PoolConfiguration(2000L, 1000L, 1000L, 6, 2, false, new Properties());
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration(configuration, poolConfiguration);
        Map<String, DataSourceConfiguration> dataSources = new HashMap<>(1);
        dataSources.put("dsc", dataSourceConfiguration);
        DataSourceGeneratedDatabaseConfiguration databaseConfiguration = new DataSourceGeneratedDatabaseConfiguration(dataSources,
                Collections.singletonList(new FixtureRuleConfiguration("rule0")));
        DataSource dataSource = databaseConfiguration.getDataSources().get("dsc");
        assertThat(dataSource, instanceOf(HikariDataSource.class));
        assertThat(((HikariDataSource) dataSource).getUsername(), is(username));
        assertThat(((HikariDataSource) dataSource).getPassword(), is(password));
        assertThat(((HikariDataSource) dataSource).getJdbcUrl(), is(url));
        FixtureRuleConfiguration next =
                (FixtureRuleConfiguration) databaseConfiguration.getRuleConfigurations().iterator().next();
        assertThat(next.getName(), is("rule0"));
        DataSourceProperties properties = databaseConfiguration.getDataSourceProperties().get("dsc");
        assertThat(properties.getPoolPropertySynonyms().getStandardPropertyKeys().size(), is(6));
        assertThat(properties.getConnectionPropertySynonyms().getStandardProperties().get("url"), is(url));
        assertThat(properties.getConnectionPropertySynonyms().getStandardProperties().get("username"), is(username));
        assertThat(properties.getConnectionPropertySynonyms().getStandardProperties().get("password"), is(password));
    }
}
