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

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;
import org.apache.shardingsphere.agent.core.plugin.yaml.loader.YamlAdvisorsConfigurationLoader;
import org.apache.shardingsphere.agent.core.plugin.yaml.swapper.YamlAdvisorsConfigurationSwapper;
import org.apache.shardingsphere.agent.core.spi.PluginServiceLoader;
import org.apache.shardingsphere.agent.spi.plugin.PluginBootService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Advisor configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdvisorConfigurationLoader {
    
    private static final YamlAdvisorsConfigurationLoader LOADER = new YamlAdvisorsConfigurationLoader();
    
    private static final YamlAdvisorsConfigurationSwapper SWAPPER = new YamlAdvisorsConfigurationSwapper();
    
    /**
     * Load advisor configurations.
     * 
     * @param pluginJars plugin jars
     * @param pluginTypes plugin types
     * @param isEnhancedForProxy is enhanced for proxy
     * @return loaded advisor configurations
     */
    public static Map<String, AdvisorConfiguration> load(final Collection<PluginJar> pluginJars, final Collection<String> pluginTypes, final boolean isEnhancedForProxy) {
        Map<String, AdvisorConfiguration> result = new HashMap<>();
        AgentClassLoader.init(pluginJars);
        for (PluginBootService each : PluginServiceLoader.newServiceInstances(PluginBootService.class, AgentClassLoader.getClassLoader())) {
            if (pluginTypes.contains(each.getType())) {
                String resourceFile = String.join("/", "", each.getType().toLowerCase(), (isEnhancedForProxy ? "proxy" : "jdbc") + "-advisors.yaml");
                Collection<AdvisorConfiguration> advisorConfigs = SWAPPER.swapToObject(LOADER.load(each.getClass().getResourceAsStream(resourceFile)), each.getType());
                result.putAll(advisorConfigs.stream().collect(Collectors.toMap(AdvisorConfiguration::getTargetClassName, Function.identity())));
            }
        }
        return ImmutableMap.<String, AdvisorConfiguration>builder().putAll(result).build();
    }
}
