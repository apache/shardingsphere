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

package org.apache.shardingsphere.agent.core.transformer.build.advise;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginBootServiceManager;
import org.apache.shardingsphere.agent.core.plugin.PluginJarHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC advice factory.
 */
@RequiredArgsConstructor
public final class JDBCAdviceFactory {
    
    private static final Map<String, Object> CACHED_ADVICES = new ConcurrentHashMap<>();
    
    private static final Map<ClassLoader, ClassLoader> PLUGIN_CLASS_LOADERS = new HashMap<>();
    
    private static boolean isStarted;
    
    private final ClassLoader classLoader;
    
    private final Map<String, PluginConfiguration> pluginConfigs;
    
    /**
     * Get advice.
     *
     * @param adviceClassName advice class name
     * @param <T> type of advice
     * @return got advance
     */
    @SuppressWarnings("unchecked")
    public <T> T getAdvice(final String adviceClassName) {
        String adviceInstanceCacheKey = String.format("%s_%s@%s", adviceClassName, classLoader.getClass().getName(), Integer.toHexString(classLoader.hashCode()));
        return (T) CACHED_ADVICES.computeIfAbsent(adviceInstanceCacheKey, key -> createAdviceForJDBC(adviceClassName));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object createAdviceForJDBC(final String adviceClassName) {
        ClassLoader pluginClassLoader = PLUGIN_CLASS_LOADERS.computeIfAbsent(classLoader, key -> new AgentClassLoader(key, PluginJarHolder.getPluginJars()));
        Object result = Class.forName(adviceClassName, true, pluginClassLoader).getDeclaredConstructor().newInstance();
        setupPluginBootService(pluginClassLoader);
        return result;
    }
    
    private void setupPluginBootService(final ClassLoader pluginClassLoader) {
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
