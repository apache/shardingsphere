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

package org.apache.shardingsphere.agent.core.yaml.swapper;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.core.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.spi.AgentServiceLoader;
import org.apache.shardingsphere.agent.core.spi.AgentTypedSPIRegistry;
import org.apache.shardingsphere.agent.core.yaml.config.YamlPluginConfiguration;

/**
 * YAML plugin configuration swapper engine.
 */
@SuppressWarnings("ALL")
public final class YamlPluginConfigurationSwapperEngine {
    
    /**
     * Swap from YAML plugin configurations to plugin configurations.
     *
     * @param yamlPluginConfigurations YAML plugin configurations
     * @return plugin configurations
     */
    public static Collection<PluginConfiguration> swapToPluginConfigurations(final Collection<YamlPluginConfiguration> yamlPluginConfigurations) {
        Collection<PluginConfiguration> result = new LinkedList<>();
        Collection<String> ruleConfigurationTypes = yamlPluginConfigurations.stream().map(YamlPluginConfiguration::getPluginName).collect(Collectors.toList());
        for (Entry<String, YamlPluginConfigurationSwapper> entry : AgentTypedSPIRegistry.getRegisteredServices(ruleConfigurationTypes, YamlPluginConfigurationSwapper.class).entrySet()) {
            result.addAll(swapToRuleConfigurations(yamlPluginConfigurations, entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private static Collection<PluginConfiguration> swapToRuleConfigurations(final Collection<YamlPluginConfiguration> yamlPluginConfigurations,
                                                                   final String type, final YamlPluginConfigurationSwapper swapper) {
        Collection<PluginConfiguration> result = new LinkedList<>();
        for (YamlPluginConfiguration each : yamlPluginConfigurations) {
            if (each.getPluginName().equals(type)) {
                result.add((PluginConfiguration) swapper.swapToObject(each));
            }
        }
        return result;
    }
    
    /**
     * Get YAML shortcuts.
     * 
     * @return YAML shortcuts
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Map<String, Class<?>> getYamlShortcuts() {
        Map<String, Class<?>> result = new HashMap<>();
        for (YamlPluginConfigurationSwapper each : AgentServiceLoader.getServiceLoader(YamlPluginConfigurationSwapper.class).newServiceInstances()) {
            Class<?> yamlRuleConfigurationClass = Class.forName(((ParameterizedType) each.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].getTypeName());
            result.put("!" + each.getPluginTagName(), yamlRuleConfigurationClass);
        }
        return result;
    }
}
