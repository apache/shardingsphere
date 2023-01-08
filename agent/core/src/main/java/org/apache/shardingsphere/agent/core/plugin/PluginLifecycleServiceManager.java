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
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.log.LoggerFactory;
import org.apache.shardingsphere.agent.core.log.LoggerFactory.Logger;
import org.apache.shardingsphere.agent.core.plugin.jar.PluginJar;
import org.apache.shardingsphere.agent.core.spi.AgentServiceLoader;
import org.apache.shardingsphere.agent.spi.PluginLifecycleService;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Plugin lifecycle service manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginLifecycleServiceManager {
    
    private static final AtomicBoolean STARTED_FLAG = new AtomicBoolean(false);
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginLifecycleServiceManager.class);
    
    /**
     * Initialize all plugins.
     * 
     * @param pluginConfigs plugin configuration map
     * @param pluginJars plugin jars
     * @param agentClassLoader agent class loader
     * @param isEnhancedForProxy is enhanced for proxy
     */
    public static void init(final Map<String, PluginConfiguration> pluginConfigs, final Collection<PluginJar> pluginJars, final ClassLoader agentClassLoader, final boolean isEnhancedForProxy) {
        if (STARTED_FLAG.compareAndSet(false, true)) {
            PluginLifecycleServiceManager.start(pluginConfigs, agentClassLoader, isEnhancedForProxy);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> PluginLifecycleServiceManager.close(pluginJars)));
        }
    }
    
    private static void start(final Map<String, PluginConfiguration> pluginConfigs, final ClassLoader agentClassLoader, final boolean isEnhancedForProxy) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(agentClassLoader);
            for (Entry<String, PluginConfiguration> entry : pluginConfigs.entrySet()) {
                AgentServiceLoader.getServiceLoader(PluginLifecycleService.class).getServices()
                        .stream().filter(each -> each.getType().equalsIgnoreCase(entry.getKey())).findFirst().ifPresent(optional -> start(entry.getValue(), optional, isEnhancedForProxy));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    private static void start(final PluginConfiguration pluginConfig, final PluginLifecycleService pluginLifecycleService, final boolean isEnhancedForProxy) {
        try {
            LOGGER.info("Start plugin: {}", pluginLifecycleService.getType());
            pluginLifecycleService.start(pluginConfig, isEnhancedForProxy);
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.error("Failed to start service.", ex);
        }
    }
    
    private static void close(final Collection<PluginJar> pluginJars) {
        AgentServiceLoader.getServiceLoader(PluginLifecycleService.class).getServices().forEach(each -> {
            try {
                each.close();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to close service.", ex);
            }
        });
        pluginJars.forEach(each -> {
            try {
                each.getJarFile().close();
            } catch (final IOException ex) {
                LOGGER.error("Failed to close jar file.", ex);
            }
        });
    }
}
