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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collection;
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
        DataSourceGeneratedDatabaseConfiguration actual = createDatabaseConfiguration(MockedDataSource.class.getName());
        assertRuleConfigurations(actual.getRuleConfigurations());
        assertStorageUnits(actual.getStorageUnits().get("foo_db"));
        assertDataSources((MockedDataSource) actual.getDataSources().get(new StorageNode("foo_db")));
    }
    
    private void assertRuleConfigurations(final Collection<RuleConfiguration> actual) {
        FixtureRuleConfiguration ruleConfig = (FixtureRuleConfiguration) actual.iterator().next();
        assertThat(ruleConfig.getName(), is("foo_rule"));
    }
    
    private void assertStorageUnits(final StorageUnit actual) {
        DataSource dataSource = actual.getDataSource();
        assertThat(dataSource, isA(MockedDataSource.class));
        assertPoolProperties(actual.getDataSourcePoolProperties().getPoolPropertySynonyms().getStandardProperties());
        assertConnectionProperties(actual.getDataSourcePoolProperties().getConnectionPropertySynonyms().getStandardProperties());
    }
    
    private void assertPoolProperties(final Map<String, Object> actual) {
        assertThat(actual.size(), is(6));
        assertThat(actual.get("connectionTimeoutMilliseconds"), is(2000L));
        assertThat(actual.get("idleTimeoutMilliseconds"), is(1000L));
        assertThat(actual.get("maxLifetimeMilliseconds"), is(1000L));
        assertThat(actual.get("maxPoolSize"), is(2));
        assertThat(actual.get("minPoolSize"), is(1));
        assertFalse((Boolean) actual.get("readOnly"));
    }
    
    private void assertConnectionProperties(final Map<String, Object> actual) {
        assertThat(actual.size(), is(4));
        assertThat(actual.get("dataSourceClassName"), is(MockedDataSource.class.getName()));
        assertThat(actual.get("url"), is("jdbc:mock://127.0.0.1/foo_db"));
        assertThat(actual.get("username"), is("root"));
        assertThat(actual.get("password"), is(""));
    }
    
    private void assertDataSources(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_db"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is(""));
    }
    
    @Test
    void assertNewWithException() {
        assertThrows(Exception.class, () -> createDatabaseConfiguration("non.existent.DataSourceClass"));
    }
    
    private DataSourceGeneratedDatabaseConfiguration createDatabaseConfiguration(final String dataSourceClassName) {
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration(new ConnectionConfiguration(dataSourceClassName, "jdbc:mock://127.0.0.1/foo_db", "root", ""),
                new PoolConfiguration(2000L, 1000L, 1000L, 2, 1, false, new Properties()));
        return new DataSourceGeneratedDatabaseConfiguration(Collections.singletonMap("foo_db", dataSourceConfig), Collections.singleton(new FixtureRuleConfiguration("foo_rule")), true);
    }
}
