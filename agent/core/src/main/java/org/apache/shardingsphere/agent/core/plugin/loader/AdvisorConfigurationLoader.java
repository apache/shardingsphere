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
import org.apache.shardingsphere.agent.config.advisor.ClassAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;
import org.apache.shardingsphere.agent.core.spi.PluginServiceLoader;
import org.apache.shardingsphere.agent.spi.advisor.AdvisorDefinitionService;

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
    
    /**
     * Load advisor configurations.
     * 
     * @param pluginJars plugin jars
     * @param pluginTypes plugin types
     * @param isEnhancedForProxy is enhanced for proxy
     * @return loaded advisor configurations
     */
    public static Map<String, ClassAdvisorConfiguration> load(final Collection<PluginJar> pluginJars, final Collection<String> pluginTypes, final boolean isEnhancedForProxy) {
        Map<String, ClassAdvisorConfiguration> result = new HashMap<>();
        AgentClassLoader.init(pluginJars);
        for (AdvisorDefinitionService each : PluginServiceLoader.newServiceInstances(AdvisorDefinitionService.class, AgentClassLoader.getClassLoader())) {
            if (pluginTypes.contains(each.getType())) {
                Collection<ClassAdvisorConfiguration> advisorConfigs = isEnhancedForProxy ? each.getProxyAdvisorConfigurations() : each.getJDBCAdvisorConfigurations();
                result.putAll(advisorConfigs.stream().collect(Collectors.toMap(ClassAdvisorConfiguration::getTargetClassName, Function.identity())));
            }
        }
        return ImmutableMap.<String, ClassAdvisorConfiguration>builder().putAll(result).build();
    }
}
