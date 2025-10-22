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

import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSourceGeneratedDatabaseConfigurationTest {
    
    @Test
    void assertNewSuccess() {
        DataSourceGeneratedDatabaseConfiguration actual = createDatabaseConfiguration(createDataSourceConfiguration(MockedDataSource.class.getName()));
        assertDataSource(actual);
        assertDataSources(actual);
        assertRuleConfigurations(actual);
        assertDataSourcePoolProperties(actual);
    }
    
    private void assertDataSource(final DataSourceGeneratedDatabaseConfiguration actual) {
        assertThat(actual.getStorageUnits().get("foo_db").getDataSource(), isA(MockedDataSource.class));
    }
    
    private void assertDataSources(final DataSourceGeneratedDatabaseConfiguration actual) {
        MockedDataSource mockedDataSource = (MockedDataSource) actual.getDataSources().get(new StorageNode("foo_db"));
        assertThat(mockedDataSource.getUrl(), is("jdbc:mock://127.0.0.1/foo_db"));
        assertThat(mockedDataSource.getUsername(), is("root"));
        assertThat(mockedDataSource.getPassword(), is(""));
    }
    
    private void assertRuleConfigurations(final DataSourceGeneratedDatabaseConfiguration actual) {
        FixtureRuleConfiguration ruleConfig = (FixtureRuleConfiguration) actual.getRuleConfigurations().iterator().next();
        assertThat(ruleConfig.getName(), is("foo_rule"));
    }
    
    private void assertDataSourcePoolProperties(final DataSourceGeneratedDatabaseConfiguration actual) {
        DataSourcePoolProperties props = actual.getStorageUnits().get("foo_db").getDataSourcePoolProperties();
        assertPoolProperties(props);
        assertConnectionProperties(props);
    }
    
    private void assertPoolProperties(final DataSourcePoolProperties actual) {
        Map<String, Object> poolStandardProps = actual.getPoolPropertySynonyms().getStandardProperties();
        assertThat(poolStandardProps.size(), is(6));
        assertThat(poolStandardProps.get("connectionTimeoutMilliseconds"), is(2000L));
        assertThat(poolStandardProps.get("idleTimeoutMilliseconds"), is(1000L));
        assertThat(poolStandardProps.get("maxLifetimeMilliseconds"), is(1000L));
        assertThat(poolStandardProps.get("maxPoolSize"), is(2));
        assertThat(poolStandardProps.get("minPoolSize"), is(1));
        assertFalse((Boolean) poolStandardProps.get("readOnly"));
    }
    
    private void assertConnectionProperties(final DataSourcePoolProperties actual) {
        Map<String, Object> connStandardProps = actual.getConnectionPropertySynonyms().getStandardProperties();
        assertThat(connStandardProps.size(), is(4));
        assertThat(connStandardProps.get("dataSourceClassName"), is(MockedDataSource.class.getName()));
        assertThat(connStandardProps.get("url"), is("jdbc:mock://127.0.0.1/foo_db"));
        assertThat(connStandardProps.get("username"), is("root"));
        assertThat(connStandardProps.get("password"), is(""));
    }
    
    @Test
    void assertNewWithException() {
        assertThrows(Exception.class, () -> createDatabaseConfiguration(createDataSourceConfiguration("non.existent.DataSourceClass")));
    }
    
    private DataSourceGeneratedDatabaseConfiguration createDatabaseConfiguration(final DataSourceConfiguration dataSourceConfig) {
        return new DataSourceGeneratedDatabaseConfiguration(Collections.singletonMap("foo_db", dataSourceConfig), Collections.singleton(new FixtureRuleConfiguration("foo_rule")));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration(final String dataSourceClassName) {
        return new DataSourceConfiguration(new ConnectionConfiguration(dataSourceClassName, "jdbc:mock://127.0.0.1/foo_db", "root", ""),
                new PoolConfiguration(2000L, 1000L, 1000L, 2, 1, false, new Properties()));
    }
}
