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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.proxy.config.resource.ProxyResourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.resource.ProxyResourceConfigurationConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxySchemaConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * YAML proxy configuration swapper.
 */
public final class YamlProxyConfigurationSwapper {
    
    /**
     * Swap YAML proxy configuration to proxy configuration.
     * 
     * @param yamlConfig YAML proxy configuration
     * @return proxy configuration
     */
    public ProxyConfiguration swap(final YamlProxyConfiguration yamlConfig) {
        Map<String, Map<String, ProxyResourceConfiguration>> schemaResourceConfigs = getResourceConfigurationMap(yamlConfig.getSchemaConfigurations());
        Map<String, Collection<RuleConfiguration>> schemaConfigs = getSchemaConfigurations(yamlConfig.getSchemaConfigurations());
        Collection<RuleConfiguration> globalRules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yamlConfig.getServerConfiguration().getRules());
        Properties props = yamlConfig.getServerConfiguration().getProps();
        return new ProxyConfiguration(schemaResourceConfigs, schemaConfigs, globalRules, props, yamlConfig.getServerConfiguration().getLabels());
    }
    
    private Map<String, Collection<RuleConfiguration>> getSchemaConfigurations(final Map<String, YamlProxySchemaConfiguration> yamlSchemaConfigs) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return yamlSchemaConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, 
            entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, Map<String, ProxyResourceConfiguration>> getResourceConfigurationMap(final Map<String, YamlProxySchemaConfiguration> yamlSchemaConfigs) {
        return yamlSchemaConfigs.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> ProxyResourceConfigurationConverter.getResourceConfigurationMap(entry.getValue().getDataSources()),
                    (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
