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

package org.apache.shardingsphere.agent.core.plugin.advisor;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
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
 * Agent advisors.
 */
public final class AgentAdvisors {
    
    private final Map<String, ClassAdvisorConfiguration> advisorConfigs;
    
    @Setter
    private boolean isEnhancedForProxy = true;
    
    public AgentAdvisors(final Collection<String> pluginTypes, final Collection<PluginJar> pluginJars) {
        AgentClassLoader.init(pluginJars);
        advisorConfigs = getAllAdvisorConfigurations(pluginTypes, AgentClassLoader.getClassLoader());
    }
    
    private Map<String, ClassAdvisorConfiguration> getAllAdvisorConfigurations(final Collection<String> pluginTypes, final ClassLoader classLoader) {
        Map<String, ClassAdvisorConfiguration> result = new HashMap<>();
        for (AdvisorDefinitionService each : PluginServiceLoader.newServiceInstances(AdvisorDefinitionService.class, classLoader)) {
            if (pluginTypes.contains(each.getType())) {
                Collection<ClassAdvisorConfiguration> advisorConfigs = isEnhancedForProxy ? each.getProxyAdvisorConfigurations() : each.getJDBCAdvisorConfigurations();
                result.putAll(advisorConfigs.stream().collect(Collectors.toMap(ClassAdvisorConfiguration::getTargetClassName, Function.identity())));
            }
        }
        return ImmutableMap.<String, ClassAdvisorConfiguration>builder().putAll(result).build();
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
                return advisorConfigs.containsKey(target.getTypeName());
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
        return advisorConfigs.containsKey(typeDescription.getTypeName());
    }
    
    /**
     * Get class advisor configuration.
     *
     * @param typeDescription type description
     * @return class advisor configuration
     */
    public ClassAdvisorConfiguration getClassAdvisorConfiguration(final TypeDescription typeDescription) {
        return advisorConfigs.getOrDefault(typeDescription.getTypeName(), new ClassAdvisorConfiguration(""));
    }
}
