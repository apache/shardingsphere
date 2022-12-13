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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.common.AgentClassLoader;
import org.apache.shardingsphere.agent.core.config.registry.AgentConfigurationRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Advice instance loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdviceInstanceLoader {
    
    private static final Map<String, Object> ADVICE_INSTANCE_CACHE = new ConcurrentHashMap<>();
    
    private static final Map<ClassLoader, ClassLoader> PLUGIN_CLASSLOADERS = new HashMap<>();
    
    private static final ReentrantLock INIT_INSTANCE_LOCK = new ReentrantLock();
    
    private static boolean isStarted;
    
    /**
     * Load instance of advice class.
     *
     * @param <T> expected type
     * @param className class name
     * @param classLoader classloader
     * @param isEnhancedForProxy is enhanced for proxy
     * @return the type reference
     */
    public static <T> T loadAdviceInstance(final String className, final ClassLoader classLoader, final boolean isEnhancedForProxy) {
        return isEnhancedForProxy ? loadAdviceInstanceForProxy(className) : loadAdviceInstanceForJdbc(className, classLoader);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private static <T> T loadAdviceInstanceForProxy(final String className) {
        Object adviceInstance = ADVICE_INSTANCE_CACHE.get(className);
        if (Objects.nonNull(adviceInstance)) {
            return (T) adviceInstance;
        }
        try {
            INIT_INSTANCE_LOCK.lock();
            adviceInstance = ADVICE_INSTANCE_CACHE.get(className);
            if (Objects.isNull(adviceInstance)) {
                adviceInstance = Class.forName(className, true, AgentClassLoader.getDefaultPluginClassloader()).getDeclaredConstructor().newInstance();
                ADVICE_INSTANCE_CACHE.put(className, adviceInstance);
            }
            return (T) adviceInstance;
        } finally {
            INIT_INSTANCE_LOCK.unlock();
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private static <T> T loadAdviceInstanceForJdbc(final String className, final ClassLoader classLoader) {
        String adviceInstanceCacheKey = String.format("%s_%s@%s", className, classLoader.getClass().getName(), Integer.toHexString(classLoader.hashCode()));
        Object adviceInstance = ADVICE_INSTANCE_CACHE.get(adviceInstanceCacheKey);
        if (Objects.nonNull(adviceInstance)) {
            return (T) adviceInstance;
        }
        INIT_INSTANCE_LOCK.lock();
        try {
            adviceInstance = ADVICE_INSTANCE_CACHE.get(adviceInstanceCacheKey);
            ClassLoader pluginClassLoader = PLUGIN_CLASSLOADERS.get(classLoader);
            if (Objects.isNull(adviceInstance)) {
                if (Objects.isNull(pluginClassLoader)) {
                    pluginClassLoader = new AgentClassLoader(classLoader, PluginJarHolder.getPluginJars());
                    PLUGIN_CLASSLOADERS.put(classLoader, pluginClassLoader);
                }
                adviceInstance = Class.forName(className, true, pluginClassLoader).getDeclaredConstructor().newInstance();
                ADVICE_INSTANCE_CACHE.put(adviceInstanceCacheKey, adviceInstance);
            }
            setupPluginBootService(pluginClassLoader);
            return (T) adviceInstance;
        } finally {
            INIT_INSTANCE_LOCK.unlock();
        }
    }
    
    private static void setupPluginBootService(final ClassLoader classLoader) {
        if (isStarted) {
            return;
        }
        try {
            PluginBootServiceManager.startAllServices(AgentConfigurationRegistry.INSTANCE.get(AgentConfiguration.class).getPlugins(), classLoader, false);
            Runtime.getRuntime().addShutdownHook(new Thread(PluginBootServiceManager::closeAllServices));
        } finally {
            isStarted = true;
        }
    }
}
