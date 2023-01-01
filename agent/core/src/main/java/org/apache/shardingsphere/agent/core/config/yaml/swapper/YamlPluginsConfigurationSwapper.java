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

package org.apache.shardingsphere.agent.core.config.yaml.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.config.yaml.entity.YamlPluginCategoryConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.entity.YamlPluginConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.entity.YamlPluginsConfiguration;
import org.apache.shardingsphere.agent.api.PluginConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    public static Map<String, PluginConfiguration> swap(final YamlPluginsConfiguration yamlConfig) {
        Map<String, PluginConfiguration> result = new LinkedHashMap<>();
        YamlPluginCategoryConfiguration plugins = yamlConfig.getPlugins();
        if (null != plugins) {
            result.putAll(transformPluginConfigurationMap(plugins.getLogging()));
            result.putAll(transformPluginConfigurationMap(plugins.getMetrics()));
            result.putAll(transformPluginConfigurationMap(plugins.getTracing()));
        }
        return result;
    }
    
    private static Map<String, PluginConfiguration> transformPluginConfigurationMap(final Map<String, YamlPluginConfiguration> yamlConfigurationMap) {
        Map<String, PluginConfiguration> result = new LinkedHashMap<>();
        if (null != yamlConfigurationMap) {
            for (Entry<String, YamlPluginConfiguration> entry : yamlConfigurationMap.entrySet()) {
                result.put(entry.getKey(), transform(entry.getValue()));
            }
        }
        return result;
    }
    
    private static PluginConfiguration transform(final YamlPluginConfiguration yamlConfig) {
        return new PluginConfiguration(yamlConfig.getHost(), yamlConfig.getPort(), yamlConfig.getPassword(), yamlConfig.getProps());
    }
}
