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

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlHeartbeatConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
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

import static org.hamcrest.core.Is.is;
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
        assertClusterConfiguration(proxyConfiguration);
        assertMetricsConfiguration(proxyConfiguration);
        assertProxyConfigurationProps(proxyConfiguration);
        assertSchemaDataSources(proxyConfiguration);
        assertSchemaRules(proxyConfiguration);
    }

    private void assertSchemaDataSources(ProxyConfiguration proxyConfiguration) {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = proxyConfiguration.getSchemaDataSources();
        assertNotNull(schemaDataSources);
        assertThat(1, is(schemaDataSources.size()));
        assertTrue(schemaDataSources.containsKey("yamlProxyRule1"));
        Map<String, DataSourceParameter> dataSourceParameterMap = schemaDataSources.get("yamlProxyRule1");
        DataSourceParameter dataSourceParameter = dataSourceParameterMap.get("ds1");
        assertNotNull(dataSourceParameter);
        assertThat("url1", is(dataSourceParameter.getUrl()));
        assertThat("username1", is(dataSourceParameter.getUsername()));
        assertThat("password1", is(dataSourceParameter.getPassword()));
        assertThat(1L, is(dataSourceParameter.getConnectionTimeoutMilliseconds()));
        assertThat(2L, is(dataSourceParameter.getIdleTimeoutMilliseconds()));
        assertThat(3L, is(dataSourceParameter.getMaxLifetimeMilliseconds()));
        assertThat(4, is(dataSourceParameter.getMaxPoolSize()));
        assertThat(5, is(dataSourceParameter.getMinPoolSize()));
        assertThat(6L, is(dataSourceParameter.getMaintenanceIntervalMilliseconds()));
        assertTrue(dataSourceParameter.isReadOnly());
    }

    private void assertSchemaRules(ProxyConfiguration proxyConfiguration) {
        Map<String, Collection<RuleConfiguration>> schemaRules = proxyConfiguration.getSchemaRules();
        assertNotNull(schemaRules);
        assertThat(1, is(schemaRules.size()));
        Collection<RuleConfiguration> ruleConfigurationCollection = schemaRules.get("yamlProxyRule1");
        assertNotNull(ruleConfigurationCollection);
        assertThat(1, is(ruleConfigurationCollection.size()));
        RuleConfiguration ruleConfiguration = ruleConfigurationCollection.iterator().next();
        assertNotNull(ruleConfiguration);
        assertTrue(ruleConfiguration instanceof MasterSlaveRuleConfiguration);
    }

    private void assertProxyConfigurationProps(ProxyConfiguration proxyConfiguration) {
        Properties proxyConfigurationProps = proxyConfiguration.getProps();
        assertNotNull(proxyConfigurationProps);
        assertThat(1, is(proxyConfigurationProps.size()));
        assertThat("value4", is(proxyConfigurationProps.getProperty("key4")));
    }

    private void assertMetricsConfiguration(ProxyConfiguration proxyConfiguration) {
        MetricsConfiguration metricsConfiguration = proxyConfiguration.getMetrics();
        assertNotNull(metricsConfiguration);
        assertThat("name1", is(metricsConfiguration.getMetricsName()));
        assertThat("host1", is(metricsConfiguration.getHost()));
        assertThat(111, is(metricsConfiguration.getPort()));
        assertThat(true, is(metricsConfiguration.getAsync()));
        assertThat(true, is(metricsConfiguration.getEnable()));
        assertThat(4, is(metricsConfiguration.getThreadCount()));
        Properties metricsProperties = metricsConfiguration.getProps();
        assertNotNull(metricsProperties);
        assertThat(1, is(metricsProperties.size()));
        assertThat("value3", is(metricsProperties.getProperty("key3")));
    }

    private void assertClusterConfiguration(ProxyConfiguration proxyConfiguration) {
        ClusterConfiguration clusterConfiguration = proxyConfiguration.getCluster();
        assertNotNull(clusterConfiguration);
        HeartbeatConfiguration heartbeatConfiguration = clusterConfiguration.getHeartbeat();
        assertNotNull(heartbeatConfiguration);
        assertThat("select 1;", is(heartbeatConfiguration.getSql()));
        assertThat(1, is(heartbeatConfiguration.getInterval()));
        assertTrue(heartbeatConfiguration.isRetryEnable());
        assertThat(3, is(heartbeatConfiguration.getRetryMaximum()));
        assertThat(2, is(heartbeatConfiguration.getRetryInterval()));
        assertThat(4, is(heartbeatConfiguration.getThreadCount()));
    }

    private void assertAuthentication(ProxyConfiguration proxyConfiguration) {
        Authentication authentication = proxyConfiguration.getAuthentication();
        assertNotNull(authentication);
        Map<String, ProxyUser> proxyUserMap = authentication.getUsers();
        assertThat(1, is(proxyUserMap.size()));
        ProxyUser proxyUser = proxyUserMap.get("user1");
        assertNotNull(proxyUser);
        assertThat("pass", is(proxyUser.getPassword()));
        Collection<String> authorizedSchemas = proxyUser.getAuthorizedSchemas();
        assertNotNull(authentication);
        assertThat(1, is(authorizedSchemas.size()));
        assertTrue(authorizedSchemas.contains("db1"));
    }

    private YamlProxyConfiguration getYamlProxyConfiguration() {
        YamlProxyConfiguration yamlProxyConfiguration = mock(YamlProxyConfiguration.class);
        YamlProxyServerConfiguration yamlProxyServerConfiguration = getYamlProxyServerConfiguration(yamlProxyConfiguration);
        prepareAuthentication(yamlProxyServerConfiguration);
        YamlOrchestrationConfiguration yamlOrchestrationConfiguration = prepareOrchestration(yamlProxyServerConfiguration);
        prepareRegistryCenter(yamlOrchestrationConfiguration);
        prepareAdditionalConfigCenter(yamlOrchestrationConfiguration);
        prepareCluster(yamlProxyServerConfiguration);
        prepareMetrics(yamlProxyServerConfiguration);
        prepareProps(yamlProxyServerConfiguration);
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = prepareRuleConfigurations(yamlProxyConfiguration);
        when(yamlProxyRuleConfiguration.getSchemaName()).thenReturn("ruleConfigSchema1");
        prepareDataSourceCommon(yamlProxyRuleConfiguration);
        prepareDataSource(yamlProxyRuleConfiguration);
        prepareDataSources(yamlProxyRuleConfiguration);
        prepareRules(yamlProxyRuleConfiguration);
        return yamlProxyConfiguration;
    }

    private void prepareRules(YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
        Collection<YamlRuleConfiguration> rules = new ArrayList<>();
        YamlRuleConfiguration testRuleConfiguration = new YamlMasterSlaveRuleConfiguration();
        rules.add(testRuleConfiguration);
        when(yamlProxyRuleConfiguration.getRules()).thenReturn(rules);
    }

    private void prepareDataSources(YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
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

    private void prepareDataSource(YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
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

    private void prepareDataSourceCommon(YamlProxyRuleConfiguration yamlProxyRuleConfiguration) {
        Map<String, Object> dataSourceCommon = new HashMap<>();
        when(yamlProxyRuleConfiguration.getDataSourceCommon()).thenReturn(dataSourceCommon);
    }

    private YamlProxyRuleConfiguration prepareRuleConfigurations(YamlProxyConfiguration yamlProxyConfiguration) {
        Map<String, YamlProxyRuleConfiguration> yamlProxyRuleConfigurationMap = new HashMap<>();
        when(yamlProxyConfiguration.getRuleConfigurations()).thenReturn(yamlProxyRuleConfigurationMap);
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = mock(YamlProxyRuleConfiguration.class);
        yamlProxyRuleConfigurationMap.put("yamlProxyRule1", yamlProxyRuleConfiguration);
        return yamlProxyRuleConfiguration;
    }

    private void prepareProps(YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        Properties properties = new Properties();
        properties.put("key4", "value4");
        when(yamlProxyServerConfiguration.getProps()).thenReturn(properties);
    }

    private void prepareMetrics(YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        YamlMetricsConfiguration yamlMetricsConfiguration = mock(YamlMetricsConfiguration.class);
        when(yamlProxyServerConfiguration.getMetrics()).thenReturn(yamlMetricsConfiguration);
        when(yamlMetricsConfiguration.getName()).thenReturn("name1");
        when(yamlMetricsConfiguration.getHost()).thenReturn("host1");
        when(yamlMetricsConfiguration.getPort()).thenReturn(111);
        when(yamlMetricsConfiguration.getAsync()).thenReturn(true);
        when(yamlMetricsConfiguration.getEnable()).thenReturn(true);
        when(yamlMetricsConfiguration.getThreadCount()).thenReturn(4);
        Properties yamlMetricsProperties = new Properties();
        yamlMetricsProperties.put("key3", "value3");
        when(yamlMetricsConfiguration.getProps()).thenReturn(yamlMetricsProperties);
    }

    private void prepareCluster(YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        YamlClusterConfiguration yamlClusterConfiguration = mock(YamlClusterConfiguration.class);
        YamlHeartbeatConfiguration yamlHeartbeatConfiguration = mock(YamlHeartbeatConfiguration.class);
        when(yamlClusterConfiguration.getHeartbeat()).thenReturn(yamlHeartbeatConfiguration);
        when(yamlHeartbeatConfiguration.getSql()).thenReturn("select 1;");
        when(yamlHeartbeatConfiguration.getInterval()).thenReturn(1);
        when(yamlHeartbeatConfiguration.isRetryEnable()).thenReturn(true);
        when(yamlHeartbeatConfiguration.getRetryMaximum()).thenReturn(3);
        when(yamlHeartbeatConfiguration.getRetryInterval()).thenReturn(2);
        when(yamlHeartbeatConfiguration.getThreadCount()).thenReturn(4);
        when(yamlProxyServerConfiguration.getCluster()).thenReturn(yamlClusterConfiguration);
    }

    private void prepareAdditionalConfigCenter(YamlOrchestrationConfiguration yamlOrchestrationConfiguration) {
        YamlOrchestrationCenterConfiguration additionalConfigCenterConfiguration = mock(YamlOrchestrationCenterConfiguration.class);
        when(yamlOrchestrationConfiguration.getAdditionalConfigCenter()).thenReturn(additionalConfigCenterConfiguration);
        when(additionalConfigCenterConfiguration.getType()).thenReturn("typeTwo");
        when(additionalConfigCenterConfiguration.getServerLists()).thenReturn("serverLists2");
        Properties additionalConfigCenterProperties = new Properties();
        additionalConfigCenterProperties.put("key2", "value2");
        when(additionalConfigCenterConfiguration.getProps()).thenReturn(additionalConfigCenterProperties);
        when(yamlOrchestrationConfiguration.isOverwrite()).thenReturn(true);
    }

    private void prepareRegistryCenter(YamlOrchestrationConfiguration yamlOrchestrationConfiguration) {
        YamlOrchestrationCenterConfiguration registryCenterConfiguration = mock(YamlOrchestrationCenterConfiguration.class);
        when(yamlOrchestrationConfiguration.getRegistryCenter()).thenReturn(registryCenterConfiguration);
        when(registryCenterConfiguration.getType()).thenReturn("typeOne");
        when(registryCenterConfiguration.getServerLists()).thenReturn("serverLists1");
        Properties registryCenterProperties = new Properties();
        registryCenterProperties.put("key1", "value1");
        when(registryCenterConfiguration.getProps()).thenReturn(registryCenterProperties);
        when(yamlOrchestrationConfiguration.getRegistryCenter()).thenReturn(registryCenterConfiguration);
    }

    private YamlOrchestrationConfiguration prepareOrchestration(YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        YamlOrchestrationConfiguration yamlOrchestrationConfiguration = mock(YamlOrchestrationConfiguration.class);
        when(yamlProxyServerConfiguration.getOrchestration()).thenReturn(yamlOrchestrationConfiguration);
        when(yamlOrchestrationConfiguration.getName()).thenReturn("test1");
        return yamlOrchestrationConfiguration;
    }

    private void prepareAuthentication(YamlProxyServerConfiguration yamlProxyServerConfiguration) {
        final YamlAuthenticationConfiguration yamlAuthenticationConfiguration = mock(YamlAuthenticationConfiguration.class);
        Map<String, YamlProxyUserConfiguration> yamlProxyUserConfigurationMap = new HashMap<>();
        YamlProxyUserConfiguration yamlProxyUserConfiguration = mock(YamlProxyUserConfiguration.class);
        when(yamlProxyUserConfiguration.getPassword()).thenReturn("pass");
        when(yamlProxyUserConfiguration.getAuthorizedSchemas()).thenReturn("db1");
        yamlProxyUserConfigurationMap.put("user1", yamlProxyUserConfiguration);
        when(yamlAuthenticationConfiguration.getUsers()).thenReturn(yamlProxyUserConfigurationMap);
        when(yamlProxyServerConfiguration.getAuthentication()).thenReturn(yamlAuthenticationConfiguration);
    }

    private YamlProxyServerConfiguration getYamlProxyServerConfiguration(YamlProxyConfiguration yamlProxyConfiguration) {
        YamlProxyServerConfiguration yamlProxyServerConfiguration = mock(YamlProxyServerConfiguration.class);
        when(yamlProxyConfiguration.getServerConfiguration()).thenReturn(yamlProxyServerConfiguration);
        return yamlProxyServerConfiguration;
    }
}
