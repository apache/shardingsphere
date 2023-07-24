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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlProxyConfigurationSwapperTest {
    
    @Test
    void assertSwap() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/swap");
        ProxyConfiguration actual = new YamlProxyConfigurationSwapper().swap(yamlProxyConfig);
        assertDataSources(actual);
        assertDatabaseRules(actual);
        assertAuthorityRuleConfiguration(actual);
        assertProxyConfigurationProps(actual);
    }
    
    private void assertDataSources(final ProxyConfiguration proxyConfig) {
        Map<String, DatabaseConfiguration> actual = proxyConfig.getDatabaseConfigurations();
        assertThat(actual.size(), is(1));
        HikariDataSource dataSource = (HikariDataSource) actual.get("swapper_test").getStorageResource().getStorageNodes().get("foo_db");
        assertThat(dataSource.getJdbcUrl(), is("jdbc:h2:mem:foo_db;DB_CLOSE_DELAY=-1"));
        assertThat(dataSource.getUsername(), is("sa"));
        assertThat(dataSource.getPassword(), is(""));
        assertThat(dataSource.getConnectionTimeout(), is(250L));
        assertThat(dataSource.getIdleTimeout(), is(2L));
        assertThat(dataSource.getMaxLifetime(), is(3L));
        assertThat(dataSource.getMaximumPoolSize(), is(4));
        assertThat(dataSource.getMinimumIdle(), is(5));
        assertTrue(dataSource.isReadOnly());
    }
    
    private void assertDatabaseRules(final ProxyConfiguration proxyConfig) {
        Map<String, DatabaseConfiguration> actual = proxyConfig.getDatabaseConfigurations();
        assertThat(actual.size(), is(1));
        Collection<RuleConfiguration> ruleConfigs = actual.get("swapper_test").getRuleConfigurations();
        assertThat(ruleConfigs.size(), is(1));
        assertReadwriteSplittingRuleConfiguration((ReadwriteSplittingRuleConfiguration) ruleConfigs.iterator().next());
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(1));
        ReadwriteSplittingDataSourceRuleConfiguration dataSource = actual.getDataSources().iterator().next();
        assertThat(dataSource.getName(), is("readwrite_ds"));
        assertThat(dataSource.getWriteDataSourceName(), is("foo_db"));
        assertThat(dataSource.getReadDataSourceNames(), is(Collections.singletonList("foo_db")));
        assertThat(actual.getLoadBalancers().size(), is(1));
        AlgorithmConfiguration loadBalancer = actual.getLoadBalancers().get("round_robin");
        assertThat(loadBalancer.getProps().size(), is(1));
        assertThat(loadBalancer.getProps().getProperty("foo"), is("foo_value"));
        assertThat(loadBalancer.getType(), is("ROUND_ROBIN"));
    }
    
    private void assertAuthorityRuleConfiguration(final ProxyConfiguration proxyConfig) {
        Optional<AuthorityRuleConfiguration> actual = findAuthorityRuleConfiguration(proxyConfig.getGlobalConfiguration().getRules());
        assertTrue(actual.isPresent());
        assertThat(actual.get().getUsers().size(), is(1));
        assertThat(actual.get().getUsers().iterator().next().getPassword(), is("123"));
    }
    
    private Optional<AuthorityRuleConfiguration> findAuthorityRuleConfiguration(final Collection<RuleConfiguration> globalRuleConfigs) {
        return globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration).findFirst().map(each -> (AuthorityRuleConfiguration) each);
    }
    
    private void assertProxyConfigurationProps(final ProxyConfiguration proxyConfig) {
        Properties actual = proxyConfig.getGlobalConfiguration().getProperties();
        assertThat(actual.size(), is(1));
        assertThat(actual.getProperty("bar"), is("bar_value"));
    }
}
