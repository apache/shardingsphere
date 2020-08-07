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
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YamlProxyConfigurationSwapperTest {

    @Test
    public void assertSwap() {

        YamlProxyConfiguration yamlProxyConfiguration = getYamlProxyConfiguration();

        //swap
        ProxyConfiguration proxyConfiguration = new YamlProxyConfigurationSwapper().swap(yamlProxyConfiguration);

        //test for authentication
        Authentication authentication = proxyConfiguration.getAuthentication();
        Assert.assertNotNull(authentication);
        Map<String, ProxyUser> proxyUserMap = authentication.getUsers();
        Assert.assertEquals(1, proxyUserMap.size());
        ProxyUser proxyUser = proxyUserMap.get("user1");
        Assert.assertNotNull(proxyUser);
        Assert.assertEquals("pass", proxyUser.getPassword());
        Collection<String> authorizedSchemas = proxyUser.getAuthorizedSchemas();
        Assert.assertNotNull(authentication);
        Assert.assertEquals(1, authorizedSchemas.size());
        Assert.assertTrue(authorizedSchemas.contains("db1"));

        //test for orchestration

        //test for cluster
        ClusterConfiguration clusterConfiguration = proxyConfiguration.getCluster();
        Assert.assertNotNull(clusterConfiguration);
        HeartbeatConfiguration heartbeatConfiguration = clusterConfiguration.getHeartbeat();
        Assert.assertNotNull(heartbeatConfiguration);
        Assert.assertEquals("select 1;", heartbeatConfiguration.getSql());
        Assert.assertEquals(1, heartbeatConfiguration.getInterval());
        Assert.assertTrue(heartbeatConfiguration.isRetryEnable());
        Assert.assertEquals(3, heartbeatConfiguration.getRetryMaximum());
        Assert.assertEquals(2, heartbeatConfiguration.getRetryInterval());
        Assert.assertEquals(4, heartbeatConfiguration.getThreadCount());

        //test for metrics
        MetricsConfiguration metricsConfiguration = proxyConfiguration.getMetrics();
        Assert.assertNotNull(metricsConfiguration);
        Assert.assertEquals("name1", metricsConfiguration.getMetricsName());
        Assert.assertEquals("host1", metricsConfiguration.getHost());
        Assert.assertEquals(111, metricsConfiguration.getPort());
        Assert.assertEquals(true, metricsConfiguration.getAsync());
        Assert.assertEquals(true, metricsConfiguration.getEnable());
        Assert.assertEquals(4, metricsConfiguration.getThreadCount());
        Properties metricsProperties = metricsConfiguration.getProps();
        Assert.assertNotNull(metricsProperties);
        Assert.assertEquals(1, metricsProperties.size());
        Assert.assertEquals("value3", metricsProperties.getProperty("key3"));

        //test for props
        Properties proxyConfigurationProps = proxyConfiguration.getProps();
        Assert.assertNotNull(proxyConfigurationProps);
        Assert.assertEquals(1, proxyConfigurationProps.size());
        Assert.assertEquals("value4", proxyConfigurationProps.getProperty("key4"));

        Map<String, Map<String, DataSourceParameter>> schemaDataSources = proxyConfiguration.getSchemaDataSources();
        Assert.assertNotNull(schemaDataSources);
        Assert.assertEquals(1, schemaDataSources.size());
        Assert.assertTrue(schemaDataSources.containsKey("yamlProxyRule1"));
        Map<String, DataSourceParameter> dataSourceParameterMap = schemaDataSources.get("yamlProxyRule1");
        Assert.assertTrue(dataSourceParameterMap.containsKey("ds1"));

        DataSourceParameter dataSourceParameter = dataSourceParameterMap.get("ds1");
        Assert.assertNotNull(dataSourceParameter);
        Assert.assertEquals("url1", dataSourceParameter.getUrl());
        Assert.assertEquals("username1", dataSourceParameter.getUsername());
        Assert.assertEquals("password1", dataSourceParameter.getPassword());
        Assert.assertEquals(1, dataSourceParameter.getConnectionTimeoutMilliseconds());
        Assert.assertEquals(2, dataSourceParameter.getIdleTimeoutMilliseconds());
        Assert.assertEquals(3, dataSourceParameter.getMaxLifetimeMilliseconds());
        Assert.assertEquals(4, dataSourceParameter.getMaxPoolSize());
        Assert.assertEquals(5, dataSourceParameter.getMinPoolSize());
        Assert.assertEquals(6, dataSourceParameter.getMaintenanceIntervalMilliseconds());
        Assert.assertTrue(dataSourceParameter.isReadOnly());

        Map<String, Collection<RuleConfiguration>> schemaRules = proxyConfiguration.getSchemaRules();
        Assert.assertNotNull(schemaRules);
        Assert.assertEquals(1, schemaRules.size());
        Collection<RuleConfiguration> ruleConfigurationCollection = schemaRules.get("yamlProxyRule1");
        Assert.assertNotNull(ruleConfigurationCollection);
        Assert.assertEquals(1, ruleConfigurationCollection.size());
        RuleConfiguration ruleConfiguration = ruleConfigurationCollection.iterator().next();
        Assert.assertNotNull(ruleConfiguration);
        Assert.assertTrue(ruleConfiguration instanceof MasterSlaveRuleConfiguration);
    }

    private YamlProxyConfiguration getYamlProxyConfiguration() {
        YamlProxyConfiguration yamlProxyConfiguration = mock(YamlProxyConfiguration.class);

        //serverConfiguration
        YamlProxyServerConfiguration yamlProxyServerConfiguration = mock(YamlProxyServerConfiguration.class);
        when(yamlProxyConfiguration.getServerConfiguration()).thenReturn(yamlProxyServerConfiguration);

        //prepare for authentication
        final YamlAuthenticationConfiguration yamlAuthenticationConfiguration = mock(YamlAuthenticationConfiguration.class);
        Map<String, YamlProxyUserConfiguration> yamlProxyUserConfigurationMap = new HashMap<>();
        YamlProxyUserConfiguration yamlProxyUserConfiguration = mock(YamlProxyUserConfiguration.class);
        when(yamlProxyUserConfiguration.getPassword()).thenReturn("pass");
        when(yamlProxyUserConfiguration.getAuthorizedSchemas()).thenReturn("db1");
        yamlProxyUserConfigurationMap.put("user1", yamlProxyUserConfiguration);
        when(yamlAuthenticationConfiguration.getUsers()).thenReturn(yamlProxyUserConfigurationMap);
        when(yamlProxyServerConfiguration.getAuthentication()).thenReturn(yamlAuthenticationConfiguration);

        //prepare for orchestration
        YamlOrchestrationConfiguration yamlOrchestrationConfiguration = mock(YamlOrchestrationConfiguration.class);
        when(yamlProxyServerConfiguration.getOrchestration()).thenReturn(yamlOrchestrationConfiguration);
        when(yamlOrchestrationConfiguration.getName()).thenReturn("test1");

        //registryCenter
        YamlOrchestrationCenterConfiguration registryCenterConfiguration = mock(YamlOrchestrationCenterConfiguration.class);
        when(yamlOrchestrationConfiguration.getRegistryCenter()).thenReturn(registryCenterConfiguration);
        when(registryCenterConfiguration.getType()).thenReturn("typeOne");
        when(registryCenterConfiguration.getServerLists()).thenReturn("serverLists1");
        Properties registryCenterProperties = new Properties();
        registryCenterProperties.put("key1", "value1");
        when(registryCenterConfiguration.getProps()).thenReturn(registryCenterProperties);
        when(yamlOrchestrationConfiguration.getRegistryCenter()).thenReturn(registryCenterConfiguration);

        //additionalConfigCenter
        YamlOrchestrationCenterConfiguration additionalConfigCenterConfiguration = mock(YamlOrchestrationCenterConfiguration.class);
        when(yamlOrchestrationConfiguration.getAdditionalConfigCenter()).thenReturn(additionalConfigCenterConfiguration);
        when(additionalConfigCenterConfiguration.getType()).thenReturn("typeTwo");
        when(additionalConfigCenterConfiguration.getServerLists()).thenReturn("serverLists2");
        Properties additionalConfigCenterProperties = new Properties();
        additionalConfigCenterProperties.put("key2", "value2");
        when(additionalConfigCenterConfiguration.getProps()).thenReturn(additionalConfigCenterProperties);
        when(yamlOrchestrationConfiguration.getAdditionalConfigCenter()).thenReturn(additionalConfigCenterConfiguration);

        when(yamlOrchestrationConfiguration.getAdditionalConfigCenter()).thenReturn(additionalConfigCenterConfiguration);
        when(yamlOrchestrationConfiguration.isOverwrite()).thenReturn(true);

        //prepare for cluster
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

        //prepare for metrics
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

        //prepare for props
        Properties properties = new Properties();
        properties.put("key4", "value4");
        when(yamlProxyServerConfiguration.getProps()).thenReturn(properties);

        //ruleConfigurations
        Map<String, YamlProxyRuleConfiguration> yamlProxyRuleConfigurationMap = new HashMap<>();
        when(yamlProxyConfiguration.getRuleConfigurations()).thenReturn(yamlProxyRuleConfigurationMap);

        //prepare  for ruleConfigurations
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = mock(YamlProxyRuleConfiguration.class);
        yamlProxyRuleConfigurationMap.put("yamlProxyRule1", yamlProxyRuleConfiguration);

        //schemaName
        when(yamlProxyRuleConfiguration.getSchemaName()).thenReturn("ruleConfigSchema1");

        //dataSourceCommon
        Map<String, Object> dataSourceCommon = new HashMap<>();
        when(yamlProxyRuleConfiguration.getDataSourceCommon()).thenReturn(dataSourceCommon);

        //dataSource
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

        //dataSources
        Map<String, YamlDataSourceParameter> dataSources = new HashMap<>();
        dataSources.put("ds1", yamlDataSourceParameter);
        when(yamlProxyRuleConfiguration.getDataSources()).thenReturn(dataSources);

        //rules
        Collection<YamlRuleConfiguration> rules = new ArrayList<>();
        YamlRuleConfiguration testRuleConfiguration = new YamlMasterSlaveRuleConfiguration();
        rules.add(testRuleConfiguration);
        when(yamlProxyRuleConfiguration.getRules()).thenReturn(rules);
        return yamlProxyConfiguration;
    }
}
