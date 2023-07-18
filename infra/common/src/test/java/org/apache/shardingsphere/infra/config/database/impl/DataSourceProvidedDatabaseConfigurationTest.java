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

import org.apache.shardingsphere.infra.datasource.ShardingSphereStorageDataSourceWrapper;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceProvidedDatabaseConfigurationTest {
    
    @Test
    void assertGetDataSources() {
        DataSourceProvidedDatabaseConfiguration databaseConfig = createDataSourceProvidedDatabaseConfiguration();
        DataSource dataSource = databaseConfig.getDataSources().get("foo_ds");
        assertTrue(dataSource instanceof ShardingSphereStorageDataSourceWrapper);
        ShardingSphereStorageDataSourceWrapper wrapper = (ShardingSphereStorageDataSourceWrapper) dataSource;
        assertTrue(wrapper.getDataSource() instanceof MockedDataSource);
        assertNull(wrapper.getCatalog());
    }
    
    @Test
    void assertGetStorageNodes() {
        DataSourceProvidedDatabaseConfiguration databaseConfig = createDataSourceProvidedDatabaseConfiguration();
        MockedDataSource dataSource = (MockedDataSource) databaseConfig.getStorageResource().getStorageNodes().get("foo_ds");
        assertThat(dataSource.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(dataSource.getUsername(), is("root"));
        assertThat(dataSource.getPassword(), is("root"));
    }
    
    @Test
    void assertGetStorageUnits() {
        DataSourceProvidedDatabaseConfiguration databaseConfig = createDataSourceProvidedDatabaseConfiguration();
        DataSource dataSource = databaseConfig.getDataSources().get("foo_ds");
        assertTrue(dataSource instanceof ShardingSphereStorageDataSourceWrapper);
        ShardingSphereStorageDataSourceWrapper wrapper = (ShardingSphereStorageDataSourceWrapper) dataSource;
        assertTrue(wrapper.getDataSource() instanceof MockedDataSource);
        assertNull(wrapper.getCatalog());
    }
    
    @Test
    void assertGetRuleConfigurations() {
        DataSourceProvidedDatabaseConfiguration databaseConfig = createDataSourceProvidedDatabaseConfiguration();
        FixtureRuleConfiguration ruleConfig = (FixtureRuleConfiguration) databaseConfig.getRuleConfigurations().iterator().next();
        assertThat(ruleConfig.getName(), is("test_rule"));
    }
    
    @Test
    void assertGetDataSourceProperties() {
        DataSourceProvidedDatabaseConfiguration databaseConfig = createDataSourceProvidedDatabaseConfiguration();
        DataSourceProperties props = databaseConfig.getDataSourcePropsMap().get("foo_ds");
        Map<String, Object> poolStandardProps = props.getPoolPropertySynonyms().getStandardProperties();
        assertThat(poolStandardProps.size(), is(0));
        Map<String, Object> connStandardProps = props.getConnectionPropertySynonyms().getStandardProperties();
        assertThat(connStandardProps.size(), is(3));
        assertThat(connStandardProps.get("url"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(connStandardProps.get("username"), is("root"));
        assertThat(connStandardProps.get("password"), is("root"));
    }
    
    private DataSourceProvidedDatabaseConfiguration createDataSourceProvidedDatabaseConfiguration() {
        return new DataSourceProvidedDatabaseConfiguration(createDataSources(), Collections.singletonList(new FixtureRuleConfiguration("test_rule")));
    }
    
    private Map<String, DataSource> createDataSources() {
        return Collections.singletonMap("foo_ds", new MockedDataSource());
    }
}
