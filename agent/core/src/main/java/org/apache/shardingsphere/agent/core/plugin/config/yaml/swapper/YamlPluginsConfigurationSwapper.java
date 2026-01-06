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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginCategoryConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * YAML plugins configuration swapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlPluginsConfigurationSwapper {
    
    /**
     * Swap YAML agent configuration to plugin configurations.
     *
     * @param yamlConfig YAML agent configuration
     * @return plugin configurations
     */
    public static Map<String, PluginConfiguration> swap(final YamlAgentConfiguration yamlConfig) {
        YamlPluginCategoryConfiguration plugins = yamlConfig.getPlugins();
        if (null == plugins) {
            return Collections.emptyMap();
        }
        Map<String, PluginConfiguration> result = new LinkedHashMap<>();
        result.putAll(swap(plugins.getLogging()));
        result.putAll(swap(plugins.getMetrics()));
        result.putAll(swap(plugins.getTracing()));
        return result;
    }
    
    private static Map<String, PluginConfiguration> swap(final Map<String, YamlPluginConfiguration> yamlConfigs) {
        return null == yamlConfigs
                ? Collections.emptyMap()
                : yamlConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swap(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static PluginConfiguration swap(final YamlPluginConfiguration yamlConfig) {
        return null == yamlConfig
                ? new PluginConfiguration(null, 0, null, new Properties())
                : new PluginConfiguration(yamlConfig.getHost(), yamlConfig.getPort(), yamlConfig.getPassword(), yamlConfig.getProps());
    }
}
