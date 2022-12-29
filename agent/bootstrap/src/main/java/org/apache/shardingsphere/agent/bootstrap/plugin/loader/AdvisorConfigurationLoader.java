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

package org.apache.shardingsphere.agent.bootstrap.plugin.loader;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.bootstrap.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.bootstrap.logging.LoggerFactory;
import org.apache.shardingsphere.agent.bootstrap.logging.LoggerFactory.Logger;
import org.apache.shardingsphere.agent.bootstrap.plugin.PluginJar;
import org.apache.shardingsphere.agent.bootstrap.plugin.yaml.loader.YamlAdvisorsConfigurationLoader;
import org.apache.shardingsphere.agent.bootstrap.plugin.yaml.swapper.YamlAdvisorsConfigurationSwapper;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;

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
        AgentClassLoader.init(pluginJars);
        for (String each : pluginTypes) {
            InputStream advisorsResourceStream = getAdvisorsResourceStream(each, isEnhancedForProxy);
            if (null == advisorsResourceStream) {
                LOGGER.error("No configuration of advisor for type `{}`", each);
                continue;
            }
            Collection<AdvisorConfiguration> advisorConfigs = YamlAdvisorsConfigurationSwapper.swapToObject(YamlAdvisorsConfigurationLoader.load(advisorsResourceStream), each);
            mergeAdvisorConfigurations(result, advisorConfigs);
        }
        return ImmutableMap.<String, AdvisorConfiguration>builder().putAll(result).build();
    }
    
    private static InputStream getAdvisorsResourceStream(final String type, final boolean isEnhancedForProxy) {
        InputStream result = AgentClassLoader.getClassLoader().getResourceAsStream(getAdvisorsResourceFile(type, (isEnhancedForProxy ? "proxy" : "jdbc") + "-advisors.yaml"));
        return null == result ? AgentClassLoader.getClassLoader().getResourceAsStream(getAdvisorsResourceFile(type, "advisors.yaml")) : result;
    }
    
    private static String getAdvisorsResourceFile(final String type, final String fileName) {
        return String.join("/", type.toLowerCase(), fileName);
    }
    
    private static void mergeAdvisorConfigurations(final Map<String, AdvisorConfiguration> advisorConfigMap, final Collection<AdvisorConfiguration> advisorConfigs) {
        for (AdvisorConfiguration each : advisorConfigs) {
            advisorConfigMap.computeIfAbsent(each.getTargetClassName(), key -> new AdvisorConfiguration(each.getTargetClassName())).getAdvisors().addAll(each.getAdvisors());
        }
    }
}
