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

import java.util.Collection;
import org.apache.shardingsphere.agent.core.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.yaml.swapper.YamlPluginConfigurationSwapperEngine;

/**
 * YAML agent configuration swapper.
 */
public final class YamlAgentConfigurationSwapper {
    
    /**
     * Swap YAML agent configuration to agent configuration.
     * 
     * @param yamlConfig YAML agent configuration
     * @return agent configuration
     */
    public AgentConfiguration swap(final YamlAgentConfiguration yamlConfig) {
        AgentConfiguration result = new AgentConfiguration();
        result.setApplicationName(yamlConfig.getApplicationName());
        result.setMetricsType(yamlConfig.getMetricsType());
        result.setIgnorePlugins(yamlConfig.getIgnorePlugins());
        Collection<PluginConfiguration> pluginConfigurations = YamlPluginConfigurationSwapperEngine.swapToPluginConfigurations(yamlConfig.getPlugins());
        result.setPluginConfigurations(pluginConfigurations);
        return result;
    }
}
