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

package org.apache.shardingsphere.agent.core.plugin.config.yaml.swapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginCategoryConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginConfiguration;
import org.junit.Test;

public class YamlPluginsConfigurationSwapperTest {
    
    @Test
    public void testEmptyPluginConfigurationSwap() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfiguration = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfiguration.setLogging(new TreeMap<>());
        yamlPluginCategoryConfiguration.setMetrics(new TreeMap<>());
        yamlPluginCategoryConfiguration.setTracing(new TreeMap<>());
        YamlAgentConfiguration yamlAgentConfiguration = new YamlAgentConfiguration();
        yamlAgentConfiguration.setPlugins(yamlPluginCategoryConfiguration);
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfiguration).isEmpty());
    }
    
    @Test
    public void assertTestSwapHandlesNullPlugins() {
        YamlAgentConfiguration yamlAgentConfiguration = new YamlAgentConfiguration();
        yamlAgentConfiguration.setPlugins(null);
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfiguration).isEmpty());
    }
    
    @Test
    public void assertTestSwapWithNullPlugins() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfiguration = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfiguration.setTracing(null);
        yamlPluginCategoryConfiguration.setLogging(null);
        yamlPluginCategoryConfiguration.setMetrics(null);
        YamlAgentConfiguration yamlAgentConfiguration = new YamlAgentConfiguration();
        yamlAgentConfiguration.setPlugins(yamlPluginCategoryConfiguration);
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfiguration).isEmpty());
    }
    
    @Test
    public void assertTestSwapWithSinglePluginConfiguration() {
        YamlPluginConfiguration yamlPluginConfiguration = new YamlPluginConfiguration();
        yamlPluginConfiguration.setHost("localhost");
        yamlPluginConfiguration.setPassword("random");
        yamlPluginConfiguration.setPort(8080);
        yamlPluginConfiguration.setProps(new Properties());
        Map<String, YamlPluginConfiguration> yamlPluginConfigurations = new HashMap<>();
        yamlPluginConfigurations.put("Key", yamlPluginConfiguration);
        YamlPluginCategoryConfiguration yamlPluginCategoryConfiguration = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfiguration.setLogging(yamlPluginConfigurations);
        yamlPluginCategoryConfiguration.setMetrics(new TreeMap<>());
        yamlPluginCategoryConfiguration.setTracing(new TreeMap<>());
        YamlAgentConfiguration yamlAgentConfiguration = new YamlAgentConfiguration();
        yamlAgentConfiguration.setPlugins(yamlPluginCategoryConfiguration);
        Map<String, PluginConfiguration> actualSwapResult = YamlPluginsConfigurationSwapper.swap(yamlAgentConfiguration);
        assertEquals(1, actualSwapResult.size());
        PluginConfiguration getResult = actualSwapResult.get("Key");
        assertEquals("localhost", getResult.getHost());
        assertTrue(getResult.getProps().isEmpty());
        assertEquals(8080, getResult.getPort());
        assertEquals("random", getResult.getPassword());
    }
    
    @Test
    public void assertTestSwapWithMultiplePluginConfigurations() {
        YamlPluginConfiguration yamlPluginConfiguration = new YamlPluginConfiguration();
        yamlPluginConfiguration.setHost("localhost");
        yamlPluginConfiguration.setPassword("random");
        yamlPluginConfiguration.setPort(8080);
        Properties properties = new Properties();
        yamlPluginConfiguration.setProps(properties);
        YamlPluginConfiguration yamlPluginConfiguration1 = new YamlPluginConfiguration();
        yamlPluginConfiguration1.setHost("localhost");
        yamlPluginConfiguration1.setPassword("random");
        yamlPluginConfiguration1.setPort(8080);
        yamlPluginConfiguration1.setProps(new Properties());
        Map<String, YamlPluginConfiguration> stringYamlPluginConfigurationMap = new TreeMap<>();
        stringYamlPluginConfigurationMap.put("42", yamlPluginConfiguration1);
        stringYamlPluginConfigurationMap.put("Key", yamlPluginConfiguration);
        YamlPluginCategoryConfiguration yamlPluginCategoryConfiguration = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfiguration.setLogging(stringYamlPluginConfigurationMap);
        yamlPluginCategoryConfiguration.setMetrics(new TreeMap<>());
        yamlPluginCategoryConfiguration.setTracing(new TreeMap<>());
        YamlAgentConfiguration yamlAgentConfiguration = new YamlAgentConfiguration();
        yamlAgentConfiguration.setPlugins(yamlPluginCategoryConfiguration);
        Map<String, PluginConfiguration> actualSwapResult = YamlPluginsConfigurationSwapper.swap(yamlAgentConfiguration);
        assertEquals(2, actualSwapResult.size());
        PluginConfiguration getResult = actualSwapResult.get("42");
        Properties props = getResult.getProps();
        assertEquals(properties, props);
        PluginConfiguration getResult1 = actualSwapResult.get("Key");
        assertEquals(props, getResult1.getProps());
        assertEquals(8080, getResult1.getPort());
        assertEquals("random", getResult1.getPassword());
        assertEquals("localhost", getResult1.getHost());
        assertEquals(8080, getResult.getPort());
        assertEquals("random", getResult.getPassword());
        assertEquals("localhost", getResult.getHost());
    }
}
