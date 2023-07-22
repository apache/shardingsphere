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

package org.apache.shardingsphere.infra.config.database.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.ShardingSphereStorageDataSourceWrapper;
import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceGeneratedDatabaseConfigurationTest {
    
    @Test
    void assertGetDataSources() {
        DataSourceGeneratedDatabaseConfiguration databaseConfig = createDataSourceGeneratedDatabaseConfiguration();
        DataSource dataSource = databaseConfig.getDataSources().get("normal_db");
        assertTrue(dataSource instanceof ShardingSphereStorageDataSourceWrapper);
        ShardingSphereStorageDataSourceWrapper wrapper = (ShardingSphereStorageDataSourceWrapper) dataSource;
        assertTrue(wrapper.getDataSource() instanceof HikariDataSource);
        assertNull(wrapper.getCatalog());
    }
    
    @Test
    void assertGetStorageNodes() {
        DataSourceGeneratedDatabaseConfiguration databaseConfig = createDataSourceGeneratedDatabaseConfiguration();
        HikariDataSource hikariDataSource = (HikariDataSource) databaseConfig.getStorageResource().getStorageNodes().get("normal_db");
        assertThat(hikariDataSource.getJdbcUrl(), is("jdbc:mock://127.0.0.1/normal_db"));
        assertThat(hikariDataSource.getUsername(), is("root"));
        assertThat(hikariDataSource.getPassword(), is(""));
    }
    
    @Test
    void assertGetStorageUnits() {
        DataSourceGeneratedDatabaseConfiguration databaseConfig = createDataSourceGeneratedDatabaseConfiguration();
        DataSource dataSource = databaseConfig.getDataSources().get("normal_db");
        assertTrue(dataSource instanceof ShardingSphereStorageDataSourceWrapper);
        ShardingSphereStorageDataSourceWrapper wrapper = (ShardingSphereStorageDataSourceWrapper) dataSource;
        assertTrue(wrapper.getDataSource() instanceof HikariDataSource);
        assertNull(wrapper.getCatalog());
    }
    
    @Test
    void assertGetRuleConfigurations() {
        DataSourceGeneratedDatabaseConfiguration databaseConfig = createDataSourceGeneratedDatabaseConfiguration();
        FixtureRuleConfiguration ruleConfig = (FixtureRuleConfiguration) databaseConfig.getRuleConfigurations().iterator().next();
        assertThat(ruleConfig.getName(), is("test_rule"));
    }
    
    @Test
    void assertGetDataSourceProperties() {
        DataSourceGeneratedDatabaseConfiguration databaseConfig = createDataSourceGeneratedDatabaseConfiguration();
        DataSourceProperties props = databaseConfig.getDataSourcePropsMap().get("normal_db");
        Map<String, Object> poolStandardProps = props.getPoolPropertySynonyms().getStandardProperties();
        assertThat(poolStandardProps.size(), is(6));
        assertThat(poolStandardProps.get("connectionTimeoutMilliseconds"), is(2000L));
        assertThat(poolStandardProps.get("idleTimeoutMilliseconds"), is(1000L));
        assertThat(poolStandardProps.get("maxLifetimeMilliseconds"), is(1000L));
        assertThat(poolStandardProps.get("maxPoolSize"), is(2));
        assertThat(poolStandardProps.get("minPoolSize"), is(1));
        assertFalse((Boolean) poolStandardProps.get("readOnly"));
        Map<String, Object> connStandardProps = props.getConnectionPropertySynonyms().getStandardProperties();
        assertThat(connStandardProps.size(), is(3));
        assertThat(connStandardProps.get("url"), is("jdbc:mock://127.0.0.1/normal_db"));
        assertThat(connStandardProps.get("username"), is("root"));
        assertThat(connStandardProps.get("password"), is(""));
    }
    
    private DataSourceGeneratedDatabaseConfiguration createDataSourceGeneratedDatabaseConfiguration() {
        return new DataSourceGeneratedDatabaseConfiguration(createDataSources(), Collections.singletonList(new FixtureRuleConfiguration("test_rule")));
    }
    
    private Map<String, DataSourceConfiguration> createDataSources() {
        PoolConfiguration poolConfig = new PoolConfiguration(2000L, 1000L, 1000L, 2, 1, false, new Properties());
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration(new ConnectionConfiguration("jdbc:mock://127.0.0.1/normal_db", "root", ""), poolConfig);
        return Collections.singletonMap("normal_db", dataSourceConfig);
    }
}
