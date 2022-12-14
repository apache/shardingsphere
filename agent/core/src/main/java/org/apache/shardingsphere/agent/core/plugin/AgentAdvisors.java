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

package org.apache.shardingsphere.agent.core.plugin;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.apache.shardingsphere.agent.advisor.ClassAdvisor;
import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.config.registry.AgentConfigurationRegistry;
import org.apache.shardingsphere.agent.core.plugin.loader.AdviceInstanceLoader;
import org.apache.shardingsphere.agent.core.spi.PluginServiceLoader;
import org.apache.shardingsphere.agent.spi.AdvisorDefinitionService;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agent advisors.
 */
public final class AgentAdvisors {
    
    private final Map<String, ClassAdvisor> advisors;
    
    @Setter
    private boolean isEnhancedForProxy = true;
    
    public AgentAdvisors(final Collection<PluginJar> pluginJars) {
        AgentClassLoader.init(pluginJars);
        advisors = getAllAdvisors(AgentClassLoader.getClassLoader());
    }
    
    private Map<String, ClassAdvisor> getAllAdvisors(final ClassLoader classLoader) {
        Map<String, ClassAdvisor> result = new HashMap<>();
        Collection<String> pluginTypes = getPluginTypes();
        for (AdvisorDefinitionService each : PluginServiceLoader.newServiceInstances(AdvisorDefinitionService.class, classLoader)) {
            if (pluginTypes.contains(each.getType())) {
                Collection<ClassAdvisor> advisors = isEnhancedForProxy ? each.getProxyAdvisors() : each.getJDBCAdvisors();
                result.putAll(advisors.stream().collect(Collectors.toMap(ClassAdvisor::getTargetClassName, Function.identity())));
            }
        }
        return ImmutableMap.<String, ClassAdvisor>builder().putAll(result).build();
    }
    
    private Collection<String> getPluginTypes() {
        AgentConfiguration agentConfig = AgentConfigurationRegistry.INSTANCE.get(AgentConfiguration.class);
        Collection<String> result = new HashSet<>();
        if (null != agentConfig && null != agentConfig.getPlugins()) {
            result.addAll(agentConfig.getPlugins().keySet());
        }
        return result;
    }
    
    /**
     * To find all intercepting target classes then to build type matcher.
     *
     * @return type matcher
     */
    public ElementMatcher<? super TypeDescription> createTypeMatcher() {
        return new Junction<TypeDescription>() {
            
            @SuppressWarnings("NullableProblems")
            @Override
            public boolean matches(final TypeDescription target) {
                return advisors.containsKey(target.getTypeName());
            }
            
            @SuppressWarnings("NullableProblems")
            @Override
            public <U extends TypeDescription> Junction<U> and(final ElementMatcher<? super U> other) {
                return null;
            }
            
            @SuppressWarnings("NullableProblems")
            @Override
            public <U extends TypeDescription> Junction<U> or(final ElementMatcher<? super U> other) {
                return null;
            }
        };
    }
    
    /**
     * To detect the type whether or not exists.
     *
     * @param typeDescription type description
     * @return contains when it is true
     */
    public boolean containsType(final TypeDescription typeDescription) {
        return advisors.containsKey(typeDescription.getTypeName());
    }
    
    /**
     * Load plugin advisor by type description.
     *
     * @param typeDescription type description
     * @return plugin advisor
     */
    public ClassAdvisor getPluginAdvisor(final TypeDescription typeDescription) {
        return advisors.getOrDefault(typeDescription.getTypeName(), new ClassAdvisor(""));
    }
    
    /**
     * To get or create instance of the advice class. Create new one and caching when it is not exist.
     *
     * @param adviceClassName class name of advice
     * @param classLoader class loader
     * @param <T> advice type
     * @return instance
     */
    public <T> T getOrCreateInstance(final String adviceClassName, final ClassLoader classLoader) {
        return AdviceInstanceLoader.loadAdviceInstance(adviceClassName, classLoader, isEnhancedForProxy);
    }
}
