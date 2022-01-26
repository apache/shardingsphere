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

package org.apache.shardingsphere.proxy.config.yaml.swapper;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceGeneratedSchemaConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlProxyConfigurationSwapperTest {
    
    @Test
    public void assertSwap() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/swap");
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlProxyConfig);
        assertAuthority(proxyConfig);
        assertProxyConfigurationProps(proxyConfig);
        assertSchemaDataSources(proxyConfig);
        assertSchemaRules(proxyConfig);
    }
    
    private void assertSchemaDataSources(final ProxyConfiguration proxyConfig) {
        Map<String, DataSourceGeneratedSchemaConfiguration> schemaConfigs = proxyConfig.getSchemaConfigurations();
        assertThat(schemaConfigs.size(), is(1));
        assertTrue(schemaConfigs.containsKey("yamlProxyRule1"));
        HikariDataSource dataSource = (HikariDataSource) schemaConfigs.get("yamlProxyRule1").getDataSources().get("foo_db");
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
    
    private void assertSchemaRules(final ProxyConfiguration proxyConfig) {
        Map<String, DataSourceGeneratedSchemaConfiguration> schemaConfigs = proxyConfig.getSchemaConfigurations();
        assertThat(schemaConfigs.size(), is(1));
        Collection<RuleConfiguration> ruleConfigs = schemaConfigs.get("yamlProxyRule1").getRuleConfigurations();
        assertThat(ruleConfigs.size(), is(1));
        assertThat(ruleConfigs.iterator().next(), instanceOf(ReadwriteSplittingRuleConfiguration.class));
    }
    
    private void assertProxyConfigurationProps(final ProxyConfiguration proxyConfig) {
        Properties proxyConfigurationProps = proxyConfig.getGlobalConfiguration().getProperties();
        assertThat(proxyConfigurationProps.size(), is(1));
        assertThat(proxyConfigurationProps.getProperty("key4"), is("value4"));
    }
    
    private void assertAuthority(final ProxyConfiguration proxyConfig) {
        Optional<ShardingSphereUser> user = new ShardingSphereUsers(getUsersFromAuthorityRule(proxyConfig.getGlobalConfiguration().getRules())).findUser(new Grantee("user1", ""));
        assertTrue(user.isPresent());
        assertThat(user.get().getPassword(), is("pass"));
    }
    
    private Collection<ShardingSphereUser> getUsersFromAuthorityRule(final Collection<RuleConfiguration> globalRuleConfigs) {
        for (RuleConfiguration ruleConfig : globalRuleConfigs) {
            if (ruleConfig instanceof AuthorityRuleConfiguration) {
                AuthorityRuleConfiguration authorityRuleConfiguration = (AuthorityRuleConfiguration) ruleConfig;
                return authorityRuleConfiguration.getUsers();
            }
        }
        return Collections.emptyList();
    }
}
