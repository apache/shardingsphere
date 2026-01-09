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

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceProvidedDatabaseConfigurationTest {
    
    @Test
    void assertNewWithDataSources() {
        DataSourceProvidedDatabaseConfiguration actual = new DataSourceProvidedDatabaseConfiguration(
                Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.singleton(new FixtureRuleConfiguration("foo_rule")));
        assertRuleConfigurations(actual);
        assertStorageUnits(actual.getStorageUnits().get("foo_ds"));
        assertDataSources((MockedDataSource) actual.getDataSources().get(new StorageNode("foo_ds")));
    }
    
    private void assertRuleConfigurations(final DataSourceProvidedDatabaseConfiguration actual) {
        FixtureRuleConfiguration ruleConfig = (FixtureRuleConfiguration) actual.getRuleConfigurations().iterator().next();
        assertThat(ruleConfig.getName(), is("foo_rule"));
    }
    
    @Test
    void assertNewWithStorageNodeDataSources() {
        Map<String, DataSourcePoolProperties> dataSourcePoolPropsMap = Collections.singletonMap("foo_ds", new DataSourcePoolProperties("foo_ds", createConnectionProps()));
        DataSourceProvidedDatabaseConfiguration actual = new DataSourceProvidedDatabaseConfiguration(
                Collections.singletonMap(new StorageNode("foo_ds"), new MockedDataSource()), Collections.singleton(new FixtureRuleConfiguration("foo_rule")), dataSourcePoolPropsMap, false);
        assertRuleConfigurations(actual);
        assertStorageUnits(actual.getStorageUnits().get("foo_ds"));
        assertDataSources((MockedDataSource) actual.getDataSources().get(new StorageNode("foo_ds")));
    }
    
    private Map<String, Object> createConnectionProps() {
        Map<String, Object> result = new HashMap<>(3, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
    
    private void assertStorageUnits(final StorageUnit actual) {
        DataSource dataSource = actual.getDataSource();
        assertThat(dataSource, isA(MockedDataSource.class));
        assertPoolProperties(actual.getDataSourcePoolProperties().getPoolPropertySynonyms().getStandardProperties());
        assertConnectionProperties(actual.getDataSourcePoolProperties().getConnectionPropertySynonyms().getStandardProperties());
    }
    
    private void assertPoolProperties(final Map<String, Object> actual) {
        assertTrue(actual.isEmpty());
    }
    
    private void assertConnectionProperties(final Map<String, Object> actual) {
        assertThat(actual.size(), is(3));
        assertThat(actual.get("url"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.get("username"), is("root"));
        assertThat(actual.get("password"), is("root"));
    }
    
    private void assertDataSources(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
}
