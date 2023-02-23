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

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginCategoryConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlPluginsConfigurationSwapperTest {
    
    @Test
    public void testEmptyPluginConfigurationSwap() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setLogging(new TreeMap<>());
        yamlPluginCategoryConfig.setMetrics(new TreeMap<>());
        yamlPluginCategoryConfig.setTracing(new TreeMap<>());
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfig).isEmpty());
    }
    
    @Test
    public void assertTestSwapHandlesNullPlugins() {
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(null);
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfig).isEmpty());
    }
    
    @Test
    public void assertTestSwapWithNullPlugins() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setTracing(null);
        yamlPluginCategoryConfig.setLogging(null);
        yamlPluginCategoryConfig.setMetrics(null);
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfig).isEmpty());
    }
    
    @Test
    public void assertTestSwapWithSinglePluginConfiguration() {
        YamlPluginConfiguration yamlPluginConfig = createYamlPluginConfiguration("localhost", "random", 8080, new Properties());
        Map<String, YamlPluginConfiguration> yamlPluginConfigs = new HashMap<>();
        yamlPluginConfigs.put("Key", yamlPluginConfig);
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setLogging(yamlPluginConfigs);
        yamlPluginCategoryConfig.setMetrics(new TreeMap<>());
        yamlPluginCategoryConfig.setTracing(new TreeMap<>());
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
        assertThat(actual.size(), is(1));
        PluginConfiguration pluginConfig = actual.get("Key");
        assertThat(pluginConfig.getHost(), is("localhost"));
        assertThat(pluginConfig.getPassword(), is("random"));
        assertThat(pluginConfig.getPort(), is(8080));
        assertTrue(pluginConfig.getProps().isEmpty());
    }
    
    @Test
    public void assertTestSwapWithMultiplePluginConfigurations() {
        YamlPluginConfiguration yamlPluginConfig1 = createYamlPluginConfiguration("localhost", "random", 8080, new Properties());
        YamlPluginConfiguration yamlPluginConfig2 = createYamlPluginConfiguration("localhost", "random", 8080, new Properties());
        Map<String, YamlPluginConfiguration> stringYamlPluginConfigMap = new TreeMap<>();
        stringYamlPluginConfigMap.put("42", yamlPluginConfig1);
        stringYamlPluginConfigMap.put("Key", yamlPluginConfig2);
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setLogging(stringYamlPluginConfigMap);
        yamlPluginCategoryConfig.setMetrics(new TreeMap<>());
        yamlPluginCategoryConfig.setTracing(new TreeMap<>());
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
        assertThat(actual.size(), is(2));
        PluginConfiguration pluginConfig1 = actual.get("42");
        assertThat(pluginConfig1.getHost(), is("localhost"));
        assertThat(pluginConfig1.getPassword(), is("random"));
        assertThat(pluginConfig1.getPort(), is(8080));
        assertTrue(pluginConfig1.getProps().isEmpty());
        PluginConfiguration pluginConfig2 = actual.get("Key");
        assertThat(pluginConfig2.getHost(), is("localhost"));
        assertThat(pluginConfig2.getPassword(), is("random"));
        assertThat(pluginConfig2.getPort(), is(8080));
        assertTrue(pluginConfig2.getProps().isEmpty());
    }
    
    private YamlPluginConfiguration createYamlPluginConfiguration(final String host, final String password, final int port, final Properties props) {
        YamlPluginConfiguration result = new YamlPluginConfiguration();
        result.setHost(host);
        result.setPassword(password);
        result.setPort(port);
        result.setProps(props);
        return result;
    }
}
