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

package org.apache.shardingsphere.agent.core.plugin.advice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginBootServiceManager;
import org.apache.shardingsphere.agent.core.plugin.PluginJarHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advice factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdviceFactory {
    
    private static final Map<String, Object> CACHED_ADVICES = new ConcurrentHashMap<>();
    
    private static final Map<ClassLoader, ClassLoader> PLUGIN_CLASS_LOADERS = new HashMap<>();
    
    private static boolean isStarted;
    
    /**
     * Get advice.
     *
     * @param adviceClassName advice class name
     * @param classLoader class loader
     * @param pluginConfigs plugin configurations
     * @param isEnhancedForProxy is enhanced for proxy
     * @param <T> type of advice
     * @return got advance
     */
    public static <T> T getAdvice(final String adviceClassName, final ClassLoader classLoader, final Map<String, PluginConfiguration> pluginConfigs, final boolean isEnhancedForProxy) {
        return isEnhancedForProxy ? getAdviceForProxy(adviceClassName) : getAdviceForJDBC(adviceClassName, classLoader, pluginConfigs);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getAdviceForProxy(final String className) {
        return (T) CACHED_ADVICES.computeIfAbsent(className, AdviceFactory::createAdviceForProxy);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object createAdviceForProxy(final String className) {
        return Class.forName(className, true, AgentClassLoader.getClassLoader()).getDeclaredConstructor().newInstance();
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getAdviceForJDBC(final String className, final ClassLoader classLoader, final Map<String, PluginConfiguration> pluginConfigs) {
        String adviceInstanceCacheKey = String.format("%s_%s@%s", className, classLoader.getClass().getName(), Integer.toHexString(classLoader.hashCode()));
        return (T) CACHED_ADVICES.computeIfAbsent(adviceInstanceCacheKey, key -> createAdviceForJDBC(className, classLoader, pluginConfigs));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object createAdviceForJDBC(final String className, final ClassLoader classLoader, final Map<String, PluginConfiguration> pluginConfigs) {
        ClassLoader pluginClassLoader = PLUGIN_CLASS_LOADERS.computeIfAbsent(classLoader, key -> new AgentClassLoader(key, PluginJarHolder.getPluginJars()));
        Object result = Class.forName(className, true, pluginClassLoader).getDeclaredConstructor().newInstance();
        setupPluginBootService(pluginClassLoader, pluginConfigs);
        return result;
    }
    
    private static void setupPluginBootService(final ClassLoader pluginClassLoader, final Map<String, PluginConfiguration> pluginConfigs) {
        if (isStarted) {
            return;
        }
        try {
            PluginBootServiceManager.startAllServices(pluginConfigs, pluginClassLoader, false);
            Runtime.getRuntime().addShutdownHook(new Thread(PluginBootServiceManager::closeAllServices));
        } finally {
            isStarted = true;
        }
    }
}
