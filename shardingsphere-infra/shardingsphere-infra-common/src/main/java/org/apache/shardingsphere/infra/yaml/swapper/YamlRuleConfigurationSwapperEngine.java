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

package org.apache.shardingsphere.infra.yaml.swapper;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML rule configuration swapper engine.
 */
public final class YamlRuleConfigurationSwapperEngine {
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    /**
     * Swap to YAML rule configurations.
     * 
     * @param ruleConfigurations rule configurations
     * @return YAML rule configurations
     */
    @SuppressWarnings("unchecked")
    public Collection<YamlRuleConfiguration> swapToYamlConfigurations(final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<YamlRuleConfiguration> result = new LinkedList<>();
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : OrderedSPIRegistry.getRegisteredServices(ruleConfigurations, YamlRuleConfigurationSwapper.class).entrySet()) {
            result.add((YamlRuleConfiguration) entry.getValue().swapToYamlConfiguration(entry.getKey()));
        }
        return result;
    }
    
    /**
     * Swap from YAML rule configurations to rule configurations.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @return rule configurations
     */
    public Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        Collection<Class<?>> ruleConfigurationTypes = yamlRuleConfigs.stream().map(YamlRuleConfiguration::getRuleConfigurationType).collect(Collectors.toList());
        for (Entry<Class<?>, YamlRuleConfigurationSwapper> entry : OrderedSPIRegistry.getRegisteredServicesByClass(ruleConfigurationTypes, YamlRuleConfigurationSwapper.class).entrySet()) {
            result.addAll(swapToRuleConfigurations(yamlRuleConfigs, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigurations, 
                                                                   final Class<?> ruleConfigurationType, final YamlRuleConfigurationSwapper swapper) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        for (YamlRuleConfiguration each : yamlRuleConfigurations) {
            if (each.getRuleConfigurationType().equals(ruleConfigurationType)) {
                result.add((RuleConfiguration) swapper.swapToObject(each));
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
        for (YamlRuleConfigurationSwapper each : ShardingSphereServiceLoader.newServiceInstances(YamlRuleConfigurationSwapper.class)) {
            Class<?> yamlRuleConfigurationClass = Class.forName(((ParameterizedType) each.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].getTypeName());
            result.put("!" + each.getRuleTagName(), yamlRuleConfigurationClass);
        }
        return result;
    }
}
