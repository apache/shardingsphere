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

package org.apache.shardingsphere.agent.core.advisor.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.loader.YamlAdvisorsConfigurationLoader;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.swapper.YamlAdvisorsConfigurationSwapper;
import org.apache.shardingsphere.agent.core.log.AgentLogger;
import org.apache.shardingsphere.agent.core.log.AgentLoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.classloader.AgentPluginClassLoader;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Advisor configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdvisorConfigurationLoader {
    
    private static final AgentLogger LOGGER = AgentLoggerFactory.getAgentLogger(AdvisorConfigurationLoader.class);
    
    /**
     * Load advisor configurations.
     * 
     * @param pluginJars plugin jars
     * @param pluginTypes plugin types
     * @return loaded configurations
     */
    public static Map<String, AdvisorConfiguration> load(final Collection<JarFile> pluginJars, final Collection<String> pluginTypes) {
        Map<String, AdvisorConfiguration> result = new HashMap<>();
        AgentPluginClassLoader agentPluginClassLoader = new AgentPluginClassLoader(Thread.currentThread().getContextClassLoader(), pluginJars);
        for (String each : pluginTypes) {
            InputStream advisorsResourceStream = getResourceStream(agentPluginClassLoader, each);
            if (null == advisorsResourceStream) {
                LOGGER.info("No configuration of advisor for type `{}`.", each);
            } else {
                mergeConfigurations(result, YamlAdvisorsConfigurationSwapper.swap(YamlAdvisorsConfigurationLoader.load(advisorsResourceStream), each));
            }
        }
        return result;
    }
    
    private static InputStream getResourceStream(final ClassLoader pluginClassLoader, final String pluginType) {
        return pluginClassLoader.getResourceAsStream(String.join(File.separator, "META-INF", "conf", getFileName(pluginType)));
    }
    
    private static String getFileName(final String pluginType) {
        return String.join("-", pluginType.toLowerCase(), "advisors.yaml");
    }
    
    private static void mergeConfigurations(final Map<String, AdvisorConfiguration> advisorConfigMap, final Collection<AdvisorConfiguration> toBeMergedAdvisorConfigs) {
        for (AdvisorConfiguration each : toBeMergedAdvisorConfigs) {
            advisorConfigMap.computeIfAbsent(each.getTargetClassName(), key -> new AdvisorConfiguration(each.getTargetClassName())).getAdvisors().addAll(each.getAdvisors());
        }
    }
}
