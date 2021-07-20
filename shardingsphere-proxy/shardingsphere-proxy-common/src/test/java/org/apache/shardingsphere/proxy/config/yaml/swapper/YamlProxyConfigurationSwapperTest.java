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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlRegistryCenterConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class YamlProxyConfigurationSwapperTest {
    
    @Test
    public void assertSwap() {
        YamlProxyConfiguration yamlProxyConfig = getYamlProxyConfiguration();
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlProxyConfig);
        assertAuthority(proxyConfig);
        assertProxyConfigurationProps(proxyConfig);
        assertSchemaDataSources(proxyConfig);
        assertSchemaRules(proxyConfig);
    }
    
    private void assertSchemaDataSources(final ProxyConfiguration proxyConfig) {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = proxyConfig.getSchemaDataSources();
        assertNotNull(schemaDataSources);
        assertThat(schemaDataSources.size(), is(1));
        assertTrue(schemaDataSources.containsKey("yamlProxyRule1"));
        Map<String, DataSourceParameter> dataSourceParameterMap = schemaDataSources.get("yamlProxyRule1");
        DataSourceParameter dataSourceParameter = dataSourceParameterMap.get("ds1");
        assertNotNull(dataSourceParameter);
        assertThat(dataSourceParameter.getUrl(), is("url1"));
        assertThat(dataSourceParameter.getUsername(), is("username1"));
        assertThat(dataSourceParameter.getPassword(), is("password1"));
        assertThat(dataSourceParameter.getConnectionTimeoutMilliseconds(), is(1L));
        assertThat(dataSourceParameter.getIdleTimeoutMilliseconds(), is(2L));
        assertThat(dataSourceParameter.getMaxLifetimeMilliseconds(), is(3L));
        assertThat(dataSourceParameter.getMaxPoolSize(), is(4));
        assertThat(dataSourceParameter.getMinPoolSize(), is(5));
        assertTrue(dataSourceParameter.isReadOnly());
    }
    
    private void assertSchemaRules(final ProxyConfiguration proxyConfig) {
        Map<String, Collection<RuleConfiguration>> schemaRules = proxyConfig.getSchemaRules();
        assertNotNull(schemaRules);
        assertThat(schemaRules.size(), is(1));
        Collection<RuleConfiguration> ruleConfigs = schemaRules.get("yamlProxyRule1");
        assertNotNull(ruleConfigs);
        assertThat(ruleConfigs.size(), is(1));
        RuleConfiguration ruleConfig = ruleConfigs.iterator().next();
        assertNotNull(ruleConfig);
        assertThat(ruleConfig, instanceOf(ReadwriteSplittingRuleConfiguration.class));
    }
    
    private void assertProxyConfigurationProps(final ProxyConfiguration proxyConfig) {
        Properties proxyConfigurationProps = proxyConfig.getProps();
        assertNotNull(proxyConfigurationProps);
        assertThat(proxyConfigurationProps.size(), is(1));
        assertThat(proxyConfigurationProps.getProperty("key4"), is("value4"));
    }
    
    private void assertAuthority(final ProxyConfiguration proxyConfig) {
        Optional<ShardingSphereUser> user = new ShardingSphereUsers(getUsersFromAuthorityRule(proxyConfig.getGlobalRules())).findUser(new Grantee("user1", ""));
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
    
    private YamlProxyConfiguration getYamlProxyConfiguration() {
        YamlProxyConfiguration result = mock(YamlProxyConfiguration.class);
        YamlProxyServerConfiguration yamlProxyServerConfig = getYamlProxyServerConfiguration(result);
        prepareAuthentication(yamlProxyServerConfig);
        YamlGovernanceConfiguration yamlGovernanceConfig = prepareGovernance(yamlProxyServerConfig);
        prepareRegistryCenter(yamlGovernanceConfig);
        prepareProps(yamlProxyServerConfig);
        YamlProxyRuleConfiguration yamlProxyRuleConfig = prepareRuleConfigurations(result);
        when(yamlProxyRuleConfig.getSchemaName()).thenReturn("ruleConfigSchema1");
        prepareDataSource(yamlProxyRuleConfig);
        prepareDataSources(yamlProxyRuleConfig);
        prepareRules(yamlProxyRuleConfig);
        return result;
    }
    
    private void prepareRegistryCenter(final YamlGovernanceConfiguration yamlGovernanceConfig) {
        YamlRegistryCenterConfiguration registryCenterConfig = mock(YamlRegistryCenterConfiguration.class);
        when(yamlGovernanceConfig.getRegistryCenter()).thenReturn(registryCenterConfig);
        when(registryCenterConfig.getType()).thenReturn("typeOne");
        when(registryCenterConfig.getServerLists()).thenReturn("serverLists1");
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        when(registryCenterConfig.getProps()).thenReturn(props);
        when(yamlGovernanceConfig.getRegistryCenter()).thenReturn(registryCenterConfig);
    }

    private void prepareProps(final YamlProxyServerConfiguration yamlProxyServerConfig) {
        Properties props = new Properties();
        props.setProperty("key4", "value4");
        when(yamlProxyServerConfig.getProps()).thenReturn(props);
    }
    
    private YamlProxyRuleConfiguration prepareRuleConfigurations(final YamlProxyConfiguration yamlProxyConfig) {
        Map<String, YamlProxyRuleConfiguration> yamlProxyRuleConfigMap = new HashMap<>(1, 1);
        when(yamlProxyConfig.getRuleConfigurations()).thenReturn(yamlProxyRuleConfigMap);
        YamlProxyRuleConfiguration result = mock(YamlProxyRuleConfiguration.class);
        yamlProxyRuleConfigMap.put("yamlProxyRule1", result);
        return result;
    }
    
    private void prepareDataSources(final YamlProxyRuleConfiguration yamlProxyRuleConfig) {
        YamlDataSourceParameter yamlDataSourceParameter = mock(YamlDataSourceParameter.class);
        when(yamlDataSourceParameter.getUrl()).thenReturn("url1");
        when(yamlDataSourceParameter.getUsername()).thenReturn("username1");
        when(yamlDataSourceParameter.getPassword()).thenReturn("password1");
        when(yamlDataSourceParameter.getConnectionTimeoutMilliseconds()).thenReturn(1L);
        when(yamlDataSourceParameter.getIdleTimeoutMilliseconds()).thenReturn(2L);
        when(yamlDataSourceParameter.getMaxLifetimeMilliseconds()).thenReturn(3L);
        when(yamlDataSourceParameter.getMaxPoolSize()).thenReturn(4);
        when(yamlDataSourceParameter.getMinPoolSize()).thenReturn(5);
        when(yamlDataSourceParameter.isReadOnly()).thenReturn(true);
        Map<String, YamlDataSourceParameter> dataSources = new HashMap<>(1, 1);
        dataSources.put("ds1", yamlDataSourceParameter);
        when(yamlProxyRuleConfig.getDataSources()).thenReturn(dataSources);
    }
    
    private void prepareDataSource(final YamlProxyRuleConfiguration yamlProxyRuleConfig) {
        YamlDataSourceParameter yamlDataSourceParameter = mock(YamlDataSourceParameter.class);
        when(yamlDataSourceParameter.getUrl()).thenReturn("url");
        when(yamlDataSourceParameter.getUsername()).thenReturn("username");
        when(yamlDataSourceParameter.getPassword()).thenReturn("password");
        when(yamlDataSourceParameter.getConnectionTimeoutMilliseconds()).thenReturn(1L);
        when(yamlDataSourceParameter.getIdleTimeoutMilliseconds()).thenReturn(2L);
        when(yamlDataSourceParameter.getMaxLifetimeMilliseconds()).thenReturn(3L);
        when(yamlDataSourceParameter.getMaxPoolSize()).thenReturn(4);
        when(yamlDataSourceParameter.getMinPoolSize()).thenReturn(5);
        when(yamlDataSourceParameter.isReadOnly()).thenReturn(true);
    }
    
    private void prepareRules(final YamlProxyRuleConfiguration yamlProxyRuleConfig) {
        Collection<YamlRuleConfiguration> rules = new LinkedList<>();
        YamlRuleConfiguration testRuleConfig = new YamlReadwriteSplittingRuleConfiguration();
        rules.add(testRuleConfig);
        when(yamlProxyRuleConfig.getRules()).thenReturn(rules);
    }
    
    private YamlGovernanceConfiguration prepareGovernance(final YamlProxyServerConfiguration yamlProxyServerConfig) {
        YamlGovernanceConfiguration result = mock(YamlGovernanceConfiguration.class);
        when(yamlProxyServerConfig.getGovernance()).thenReturn(result);
        when(result.getName()).thenReturn("test1");
        return result;
    }
    
    private void prepareAuthentication(final YamlProxyServerConfiguration yamlProxyServerConfig) {
        YamlAuthorityRuleConfiguration yamlAuthorityRuleConfig = new YamlAuthorityRuleConfiguration();
        yamlAuthorityRuleConfig.setUsers(getUsers());
        YamlShardingSphereAlgorithmConfiguration provider = new YamlShardingSphereAlgorithmConfiguration();
        provider.setType("test");
        yamlAuthorityRuleConfig.setProvider(provider);
        when(yamlProxyServerConfig.getRules()).thenReturn(Collections.singletonList(yamlAuthorityRuleConfig));
    }

    private Collection<String> getUsers() {
        return Lists.newArrayList("user1@:pass");
    }

    private YamlProxyServerConfiguration getYamlProxyServerConfiguration(final YamlProxyConfiguration yamlProxyConfig) {
        YamlProxyServerConfiguration result = mock(YamlProxyServerConfiguration.class);
        when(yamlProxyConfig.getServerConfiguration()).thenReturn(result);
        return result;
    }
}
