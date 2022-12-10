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
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.common.AgentClassLoader;
import org.apache.shardingsphere.agent.core.config.path.AgentPathBuilder;
import org.apache.shardingsphere.agent.core.config.registry.AgentConfigurationRegistry;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.spi.PluginServiceLoader;
import org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Agent plugin loader.
 */
public final class AgentPluginLoader implements PluginLoader {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(AgentPluginLoader.class);
    
    private final Collection<PluginJar> pluginJars = new LinkedList<>();
    
    private Map<String, PluginInterceptorPoint> interceptorPointMap;
    
    @Getter
    @Setter
    private boolean isEnhancedForProxy = true;
    
    /**
     * Load plugin jars and interceptor points.
     *
     * @throws IOException IO exception
     */
    public void load() throws IOException {
        loadPluginJars();
        AgentClassLoader.initDefaultPluginClassLoader(pluginJars);
        loadAllPluginInterceptorPoint(AgentClassLoader.getDefaultPluginClassloader());
    }
    
    private void loadPluginJars() throws IOException {
        File[] jarFiles = AgentPathBuilder.getPluginPath().listFiles(each -> each.getName().endsWith(".jar"));
        if (Objects.isNull(jarFiles)) {
            return;
        }
        for (File each : jarFiles) {
            pluginJars.add(new PluginJar(new JarFile(each, true), each));
            LOGGER.info("Loaded jar:{}", each.getName());
        }
        PluginJarHolder.setPluginJars(pluginJars);
    }
    
    private void loadAllPluginInterceptorPoint(final ClassLoader classLoader) {
        Collection<String> pluginNames = getPluginNames();
        Map<String, PluginInterceptorPoint> pointMap = new HashMap<>();
        loadPluginDefinitionServices(pluginNames, pointMap, classLoader);
        interceptorPointMap = ImmutableMap.<String, PluginInterceptorPoint>builder().putAll(pointMap).build();
    }
    
    private Collection<String> getPluginNames() {
        AgentConfiguration agentConfig = AgentConfigurationRegistry.INSTANCE.get(AgentConfiguration.class);
        Set<String> result = new HashSet<>();
        if (null != agentConfig && null != agentConfig.getPlugins()) {
            result.addAll(agentConfig.getPlugins().keySet());
        }
        return result;
    }
    
    private void loadPluginDefinitionServices(final Collection<String> pluginNames, final Map<String, PluginInterceptorPoint> pointMap, final ClassLoader classLoader) {
        PluginServiceLoader.newServiceInstances(PluginDefinitionService.class, classLoader)
                .stream()
                .filter(each -> pluginNames.contains(each.getType()))
                .forEach(each -> buildPluginInterceptorPointMap(each, pointMap));
    }
    
    private void buildPluginInterceptorPointMap(final PluginDefinitionService pluginDefinitionService, final Map<String, PluginInterceptorPoint> pointMap) {
        pluginDefinitionService.install(isEnhancedForProxy).forEach(each -> {
            String target = each.getTargetClassName();
            if (pointMap.containsKey(target)) {
                PluginInterceptorPoint pluginInterceptorPoint = pointMap.get(target);
                pluginInterceptorPoint.getConstructorPoints().addAll(each.getConstructorPoints());
                pluginInterceptorPoint.getInstanceMethodPoints().addAll(each.getInstanceMethodPoints());
                pluginInterceptorPoint.getClassStaticMethodPoints().addAll(each.getClassStaticMethodPoints());
            } else {
                pointMap.put(target, each);
            }
        });
    }
    
    /**
     * To find all intercepting target classes then to build TypeMatcher.
     *
     * @return type matcher
     */
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return new Junction<TypeDescription>() {
            
            @Override
            public boolean matches(final TypeDescription target) {
                return interceptorPointMap.containsKey(target.getTypeName());
            }
            
            @Override
            public <U extends TypeDescription> Junction<U> and(final ElementMatcher<? super U> other) {
                return null;
            }
            
            @Override
            public <U extends TypeDescription> Junction<U> or(final ElementMatcher<? super U> other) {
                return null;
            }
        };
    }
    
    @Override
    public boolean containsType(final TypeDescription typeDescription) {
        return interceptorPointMap.containsKey(typeDescription.getTypeName());
    }
    
    @Override
    public PluginInterceptorPoint loadPluginInterceptorPoint(final TypeDescription typeDescription) {
        return interceptorPointMap.getOrDefault(typeDescription.getTypeName(), PluginInterceptorPoint.createDefault());
    }
    
    @Override
    public <T> T getOrCreateInstance(final String adviceClassName, final ClassLoader classLoader) {
        return AdviceInstanceLoader.loadAdviceInstance(adviceClassName, classLoader, isEnhancedForProxy);
    }
}
