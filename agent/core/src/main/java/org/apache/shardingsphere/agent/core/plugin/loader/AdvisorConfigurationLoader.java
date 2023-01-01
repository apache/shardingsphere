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

package org.apache.shardingsphere.agent.core.plugin.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory.Logger;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;
import org.apache.shardingsphere.agent.core.plugin.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.loader.YamlAdvisorsConfigurationLoader;
import org.apache.shardingsphere.agent.core.plugin.yaml.swapper.YamlAdvisorsConfigurationSwapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Advisor configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdvisorConfigurationLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentPluginLoader.class);
    
    /**
     * Load advisor configurations.
     * 
     * @param pluginJars plugin jars
     * @param pluginTypes plugin types
     * @param isEnhancedForProxy is enhanced for proxy
     * @return loaded configurations
     */
    public static Map<String, AdvisorConfiguration> load(final Collection<PluginJar> pluginJars, final Collection<String> pluginTypes, final boolean isEnhancedForProxy) {
        Map<String, AdvisorConfiguration> result = new HashMap<>();
        AgentClassLoader agentClassLoader = new AgentClassLoader(AdvisorConfigurationLoader.class.getClassLoader(), pluginJars);
        for (String each : pluginTypes) {
            InputStream advisorsResourceStream = getAdvisorsResourceStream(agentClassLoader, each, isEnhancedForProxy);
            if (null == advisorsResourceStream) {
                LOGGER.info("No configuration of advisor for type `{}`.", each);
            } else {
                mergeAdvisorConfigurations(result, YamlAdvisorsConfigurationSwapper.swapToObject(YamlAdvisorsConfigurationLoader.load(advisorsResourceStream), each));
            }
        }
        return result;
    }
    
    private static InputStream getAdvisorsResourceStream(final ClassLoader agentClassLoader, final String pluginType, final boolean isEnhancedForProxy) {
        InputStream accurateResourceStream = getAdvisorsResourceStream(agentClassLoader, pluginType, (isEnhancedForProxy ? "proxy" : "jdbc") + "-advisors.yaml");
        return null == accurateResourceStream ? getAdvisorsResourceStream(agentClassLoader, pluginType, "advisors.yaml") : accurateResourceStream;
    }
    
    private static InputStream getAdvisorsResourceStream(final ClassLoader agentClassLoader, final String pluginType, final String fileName) {
        return agentClassLoader.getResourceAsStream(String.join("/", pluginType.toLowerCase(), fileName));
    }
    
    private static void mergeAdvisorConfigurations(final Map<String, AdvisorConfiguration> advisorConfigMap, final Collection<AdvisorConfiguration> advisorConfigs) {
        for (AdvisorConfiguration each : advisorConfigs) {
            advisorConfigMap.computeIfAbsent(each.getTargetClassName(), key -> new AdvisorConfiguration(each.getTargetClassName())).getAdvisors().addAll(each.getAdvisors());
        }
    }
}
