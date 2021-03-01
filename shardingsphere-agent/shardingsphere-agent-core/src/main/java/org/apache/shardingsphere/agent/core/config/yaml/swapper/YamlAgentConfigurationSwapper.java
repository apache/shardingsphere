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
import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.YamlPluginConfiguration;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * YAML agent configuration swapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlAgentConfigurationSwapper {
    
    /**
     * Swap YAML agent configuration to agent configuration.
     * 
     * @param yamlConfig YAML agent configuration
     * @return agent configuration
     */
    public static AgentConfiguration swap(final YamlAgentConfiguration yamlConfig) {
        Map<String, PluginConfiguration> configurationMap = yamlConfig.getPlugins().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> transform(entry.getValue())));
        return new AgentConfiguration(yamlConfig.getApplicationName(), yamlConfig.getIgnoredPluginNames(), configurationMap);
    }
    
    private static PluginConfiguration transform(final YamlPluginConfiguration yamlConfig) {
        return new PluginConfiguration(yamlConfig.getHost(), yamlConfig.getPort(), yamlConfig.getPassword(), yamlConfig.getProps());
    }
}
