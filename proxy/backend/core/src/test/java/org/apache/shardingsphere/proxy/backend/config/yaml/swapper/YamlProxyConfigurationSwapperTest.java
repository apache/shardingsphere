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

package org.apache.shardingsphere.proxy.backend.config.yaml.swapper;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.ProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

class YamlProxyConfigurationSwapperTest {
    
    @Test
    void assertSwapServerConfiguration() throws IOException {
        YamlProxyServerConfiguration yamlConfig = YamlEngine.unmarshal(new File(getClass().getResource("/conf/swap/global.yaml").getFile()), YamlProxyServerConfiguration.class);
        YamlProxyDataSourceConfiguration yamlDataSourceConfig = new YamlProxyDataSourceConfiguration();
        yamlDataSourceConfig.setDataSourceClassName("non.existent.DataSource");
        yamlDataSourceConfig.setUrl("jdbc:h2:mem:global_ds");
        yamlConfig.setDataSources(Collections.singletonMap("global_ds", yamlDataSourceConfig));
        yamlConfig.getRules().add(yamlConfig.getAuthority());
        try (
                MockedStatic<DataSourcePoolCreator> mockedPoolCreator = mockStatic(DataSourcePoolCreator.class);
                MockedConstruction<DataSourceGeneratedDatabaseConfiguration> mockedDatabaseConfigs = mockConstruction(DataSourceGeneratedDatabaseConfiguration.class)) {
            ProxyConfigurationLoadResult actual = new YamlProxyConfigurationSwapper().swap(yamlConfig, Collections.emptyMap());
            assertThat(actual.getModeConfiguration().getType(), is("Cluster"));
            assertThat(actual.getDataSources().get("global_ds").getConnection().getDataSourceClassName(), is("non.existent.DataSource"));
            assertThat(actual.getDataSources().get("global_ds").getConnection().getUrl(), is("jdbc:h2:mem:global_ds"));
            AuthorityRuleConfiguration actualAuthorityRule = (AuthorityRuleConfiguration) actual.getRules().iterator().next();
            assertThat(actualAuthorityRule.getUsers().size(), is(1));
            assertThat(actualAuthorityRule.getUsers().iterator().next().getPassword(), is("123"));
            assertThat(actual.getProperties().size(), is(1));
            assertThat(actual.getProperties().getProperty("bar"), is("bar_value"));
            mockedPoolCreator.verifyNoInteractions();
            assertTrue(mockedDatabaseConfigs.constructed().isEmpty());
        }
    }
    
    @Test
    void assertSwapDatabaseConfiguration() throws IOException {
        YamlProxyDatabaseConfiguration yamlConfig = YamlEngine.unmarshal(
                new File(getClass().getResource("/conf/swap/database-readwrite-splitting.yaml").getFile()), YamlProxyDatabaseConfiguration.class);
        try (
                MockedStatic<DataSourcePoolCreator> mockedPoolCreator = mockStatic(DataSourcePoolCreator.class);
                MockedConstruction<DataSourceGeneratedDatabaseConfiguration> mockedDatabaseConfigs = mockConstruction(DataSourceGeneratedDatabaseConfiguration.class)) {
            ProxyDatabaseConfiguration actual = new YamlProxyConfigurationSwapper().swap(yamlConfig);
            assertThat(actual.getDatabaseName(), is("swapper_test"));
            assertDataSources(actual);
            assertThat(actual.getRules().size(), is(1));
            assertReadwriteSplittingRuleConfiguration((ReadwriteSplittingRuleConfiguration) actual.getRules().iterator().next());
            mockedPoolCreator.verifyNoInteractions();
            assertTrue(mockedDatabaseConfigs.constructed().isEmpty());
        }
    }
    
    private void assertDataSources(final ProxyDatabaseConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(1));
        DataSourceConfiguration actualDataSourceConfig = actual.getDataSources().get("foo_db");
        assertThat(actualDataSourceConfig.getConnection().getUrl(), is("jdbc:h2:mem:foo_db;DB_CLOSE_DELAY=-1"));
        assertThat(actualDataSourceConfig.getConnection().getUsername(), is("sa"));
        assertThat(actualDataSourceConfig.getConnection().getPassword(), is(""));
        assertThat(actualDataSourceConfig.getPool().getConnectionTimeoutMilliseconds(), is(250L));
        assertThat(actualDataSourceConfig.getPool().getIdleTimeoutMilliseconds(), is(30000L));
        assertThat(actualDataSourceConfig.getPool().getMaxLifetimeMilliseconds(), is(600000L));
        assertThat(actualDataSourceConfig.getPool().getMaxPoolSize(), is(5));
        assertThat(actualDataSourceConfig.getPool().getMinPoolSize(), is(4));
        assertTrue(actualDataSourceConfig.getPool().getReadOnly());
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSourceGroups().size(), is(1));
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = actual.getDataSourceGroups().iterator().next();
        assertThat(dataSourceGroupConfig.getName(), is("readwrite_ds"));
        assertThat(dataSourceGroupConfig.getWriteDataSourceName(), is("foo_db"));
        assertThat(dataSourceGroupConfig.getReadDataSourceNames(), is(Collections.singletonList("foo_db")));
        assertThat(actual.getLoadBalancers().size(), is(1));
        AlgorithmConfiguration loadBalancer = actual.getLoadBalancers().get("round_robin");
        assertThat(loadBalancer.getProps().size(), is(1));
        assertThat(loadBalancer.getProps().getProperty("foo"), is("foo_value"));
        assertThat(loadBalancer.getType(), is("ROUND_ROBIN"));
    }
}
