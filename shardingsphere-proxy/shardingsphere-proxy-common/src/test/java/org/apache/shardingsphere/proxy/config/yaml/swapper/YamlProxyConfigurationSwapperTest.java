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

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YamlProxyConfigurationSwapperTest {
    
    @Test
    public void assertSwap() {
        YamlProxyConfiguration yamlProxyConfiguration = getYamlProxyConfiguration();
        ProxyConfiguration proxyConfiguration = new YamlProxyConfigurationSwapper().swap(yamlProxyConfiguration);
        assertAuthentication(proxyConfiguration);
        assertProxyConfigurationProps(proxyConfiguration);
        assertSchemaDataSources(proxyConfiguration);
        assertSchemaRules(proxyConfiguration);
    }
    
    private void assertSchemaDataSources(final ProxyConfiguration proxyConfiguration) {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = proxyConfiguration.getSchemaDataSources();
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
        assertThat(dataSourceParameter.getMaintenanceIntervalMilliseconds(), is(6L));
        assertTrue(dataSourceParameter.isReadOnly());
    }
    
    private void assertSchemaRules(final ProxyConfiguration proxyConfiguration) {
        Map<String, Collection<RuleConfiguration>> schemaRules = proxyConfiguration.getSchemaRules();
        assertNotNull(schemaRules);
        assertThat(schemaRules.size(), is(1));
        Collection<RuleConfiguration> ruleConfigurationCollection = schemaRules.get("yamlProxyRule1");
        assertNotNull(ruleConfigurationCollection);
        assertThat(ruleConfigurationCollection.size(), is(1));
        RuleConfiguration ruleConfiguration = ruleConfigurationCollection.iterator().next();
        assertNotNull(ruleConfiguration);
        assertThat(ruleConfiguration, instanceOf(MasterSlaveRuleConfiguration.class));
    }
    
    private void assertProxyConfigurationProps(final ProxyConfiguration proxyConfiguration) {
        Properties proxyConfigurationProps = proxyConfiguration.getProps();
        assertNotNull(proxyConfigurationProps);
        assertThat(proxyConfigurationProps.size(), is(1));
        assertThat(proxyConfigurationProps.getProperty("key4"), is("value4"));
    }
    
    private void assertAuthentication(final ProxyConfiguration proxyConfiguration) {
        Authentication authentication = proxyConfiguration.getAuthentication();
        assertNotNull(authentication);
        Map<String, ProxyUser> proxyUserMap = authentication.getUsers();
        assertThat(proxyUserMap.size(), is(1));
        ProxyUser proxyUser = proxyUserMap.get("user1");
        assertNotNull(proxyUser);
        assertThat(proxyUser.getPassword(), is("pass"));
        Collection<String> authorizedSchemas = proxyUser.getAuthorizedSchemas();
        assertNotNull(authentication);
        assertThat(authorizedSchemas.size(), is(1));
        assertTrue(authorizedSchemas.contains("db1"));
    }
    
    private YamlProxyConfiguration getYamlProxyConfiguration() {
        YamlProxyConfiguration yamlProxyConfiguration = mock(YamlProxyConfiguration.class);
        YamlProxyServerConfiguration yamlProxyServerConfiguration = getYamlProxyServerConfiguration(yamlProxyConfiguration);
        prepareAuthentication(yamlProxyServerConfiguration);
        YamlOrchestrationConfiguration yamlOrchestrationConfiguration = prepareOrchestration(yamlProxyServerConfiguration);
        prepareRegistryCenter(yamlOrchestrationConfiguration);
        prepareAdditionalConfigCenter(yamlOrchestrationConfiguration);
        prepareProps(yamlProxyServerConfiguration);
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = prepareRuleConfigurations(yamlProxyConfiguration);
        when(yamlProxyRuleConfiguration.getSchemaName()).thenReturn("ruleConfigSchema1");
        prepareDataSourceCommon(yamlProxyRuleConfiguration);
        prepareDataSource(yamlProxyRuleConfiguration);
        prepareDataSources(yamlProxyRuleConfiguration);
        prepareRules(yamlProxyRuleConfiguration);
        return yamlProxyConfiguration;
    }
    
    private void prepareRules(final YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
        Collection<YamlRuleConfiguration> rules = new ArrayList<>();
        YamlRuleConfiguration testRuleConfiguration = new YamlMasterSlaveRuleConfiguration();
        rules.add(testRuleConfiguration);
        when(yamlProxyRuleConfiguration.getRules()).thenReturn(rules);
    }
    
    private void prepareDataSources(final YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
        YamlDataSourceParameter yamlDataSourceParameter = mock(YamlDataSourceParameter.class);
        when(yamlProxyRuleConfiguration.getDataSource()).thenReturn(yamlDataSourceParameter);
        when(yamlDataSourceParameter.getUrl()).thenReturn("url1");
        when(yamlDataSourceParameter.getUsername()).thenReturn("username1");
        when(yamlDataSourceParameter.getPassword()).thenReturn("password1");
        when(yamlDataSourceParameter.getConnectionTimeoutMilliseconds()).thenReturn(1L);
        when(yamlDataSourceParameter.getIdleTimeoutMilliseconds()).thenReturn(2L);
        when(yamlDataSourceParameter.getMaxLifetimeMilliseconds()).thenReturn(3L);
        when(yamlDataSourceParameter.getMaxPoolSize()).thenReturn(4);
        when(yamlDataSourceParameter.getMinPoolSize()).thenReturn(5);
        when(yamlDataSourceParameter.getMaintenanceIntervalMilliseconds()).thenReturn(6L);
        when(yamlDataSourceParameter.isReadOnly()).thenReturn(true);
        Map<String, YamlDataSourceParameter> dataSources = new HashMap<>();
        dataSources.put("ds1", yamlDataSourceParameter);
        when(yamlProxyRuleConfiguration.getDataSources()).thenReturn(dataSources);
    }
    
    private void prepareDataSource(final YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
        YamlDataSourceParameter yamlDataSourceParameter = mock(YamlDataSourceParameter.class);
        when(yamlProxyRuleConfiguration.getDataSource()).thenReturn(yamlDataSourceParameter);
        when(yamlDataSourceParameter.getUrl()).thenReturn("url");
        when(yamlDataSourceParameter.getUsername()).thenReturn("username");
        when(yamlDataSourceParameter.getPassword()).thenReturn("password");
        when(yamlDataSourceParameter.getConnectionTimeoutMilliseconds()).thenReturn(1L);
        when(yamlDataSourceParameter.getIdleTimeoutMilliseconds()).thenReturn(2L);
        when(yamlDataSourceParameter.getMaxLifetimeMilliseconds()).thenReturn(3L);
        when(yamlDataSourceParameter.getMaxPoolSize()).thenReturn(4);
        when(yamlDataSourceParameter.getMinPoolSize()).thenReturn(5);
        when(yamlDataSourceParameter.getMaintenanceIntervalMilliseconds()).thenReturn(6L);
        when(yamlDataSourceParameter.isReadOnly()).thenReturn(true);
    }
    
    private void prepareDataSourceCommon(final YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
        Map<String, Object> dataSourceCommon = new HashMap<>();
        when(yamlProxyRuleConfiguration.getDataSourceCommon()).thenReturn(dataSourceCommon);
    }
    
    private YamlProxyRuleConfiguration prepareRuleConfigurations(final YamlProxyConfiguration yamlProxyConfiguration) {
        Map<String, YamlProxyRuleConfiguration> yamlProxyRuleConfigurationMap = new HashMap<>();
        when(yamlProxyConfiguration.getRuleConfigurations()).thenReturn(yamlProxyRuleConfigurationMap);
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = mock(YamlProxyRuleConfiguration.class);
        yamlProxyRuleConfigurationMap.put("yamlProxyRule1", yamlProxyRuleConfiguration);
        return yamlProxyRuleConfiguration;
    }
    
    private void prepareProps(final YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        Properties properties = new Properties();
        properties.put("key4", "value4");
        when(yamlProxyServerConfiguration.getProps()).thenReturn(properties);
    }
    
    private void prepareAdditionalConfigCenter(final YamlOrchestrationConfiguration yamlOrchestrationConfiguration) {
        YamlOrchestrationCenterConfiguration additionalConfigCenterConfiguration = mock(YamlOrchestrationCenterConfiguration.class);
        when(yamlOrchestrationConfiguration.getAdditionalConfigCenter()).thenReturn(additionalConfigCenterConfiguration);
        when(additionalConfigCenterConfiguration.getType()).thenReturn("typeTwo");
        when(additionalConfigCenterConfiguration.getServerLists()).thenReturn("serverLists2");
        Properties additionalConfigCenterProperties = new Properties();
        additionalConfigCenterProperties.put("key2", "value2");
        when(additionalConfigCenterConfiguration.getProps()).thenReturn(additionalConfigCenterProperties);
        when(yamlOrchestrationConfiguration.isOverwrite()).thenReturn(true);
    }
    
    private void prepareRegistryCenter(final YamlOrchestrationConfiguration yamlOrchestrationConfiguration) {
        YamlOrchestrationCenterConfiguration registryCenterConfiguration = mock(YamlOrchestrationCenterConfiguration.class);
        when(yamlOrchestrationConfiguration.getRegistryCenter()).thenReturn(registryCenterConfiguration);
        when(registryCenterConfiguration.getType()).thenReturn("typeOne");
        when(registryCenterConfiguration.getServerLists()).thenReturn("serverLists1");
        Properties registryCenterProperties = new Properties();
        registryCenterProperties.put("key1", "value1");
        when(registryCenterConfiguration.getProps()).thenReturn(registryCenterProperties);
        when(yamlOrchestrationConfiguration.getRegistryCenter()).thenReturn(registryCenterConfiguration);
    }
    
    private YamlOrchestrationConfiguration prepareOrchestration(final YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        YamlOrchestrationConfiguration yamlOrchestrationConfiguration = mock(YamlOrchestrationConfiguration.class);
        when(yamlProxyServerConfiguration.getOrchestration()).thenReturn(yamlOrchestrationConfiguration);
        when(yamlOrchestrationConfiguration.getName()).thenReturn("test1");
        return yamlOrchestrationConfiguration;
    }
    
    private void prepareAuthentication(final YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        final YamlAuthenticationConfiguration yamlAuthenticationConfiguration = mock(YamlAuthenticationConfiguration.class);
        Map<String, YamlProxyUserConfiguration> yamlProxyUserConfigurationMap = new HashMap<>();
        YamlProxyUserConfiguration yamlProxyUserConfiguration = mock(YamlProxyUserConfiguration.class);
        when(yamlProxyUserConfiguration.getPassword()).thenReturn("pass");
        when(yamlProxyUserConfiguration.getAuthorizedSchemas()).thenReturn("db1");
        yamlProxyUserConfigurationMap.put("user1", yamlProxyUserConfiguration);
        when(yamlAuthenticationConfiguration.getUsers()).thenReturn(yamlProxyUserConfigurationMap);
        when(yamlProxyServerConfiguration.getAuthentication()).thenReturn(yamlAuthenticationConfiguration);
    }
    
    private YamlProxyServerConfiguration getYamlProxyServerConfiguration(final YamlProxyConfiguration yamlProxyConfiguration) {
        YamlProxyServerConfiguration yamlProxyServerConfiguration = mock(YamlProxyServerConfiguration.class);
        when(yamlProxyConfiguration.getServerConfiguration()).thenReturn(yamlProxyServerConfiguration);
        return yamlProxyServerConfiguration;
    }
}
