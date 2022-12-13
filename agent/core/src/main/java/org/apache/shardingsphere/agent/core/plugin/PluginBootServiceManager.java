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
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.spi.AgentSPIRegistry;
import org.apache.shardingsphere.agent.spi.PluginBootService;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Plugin boot service manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginBootServiceManager {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(PluginBootServiceManager.class);
    
    /**
     * Start all services.
     *
     * @param pluginConfigMap plugin configuration map
     * @param classLoader class loader
     * @param isEnhancedForProxy is enhanced for proxy
     */
    public static void startAllServices(final Map<String, PluginConfiguration> pluginConfigMap, final ClassLoader classLoader, final boolean isEnhancedForProxy) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            for (Entry<String, PluginConfiguration> entry : pluginConfigMap.entrySet()) {
                AgentSPIRegistry.getRegisteredService(PluginBootService.class, entry.getKey()).ifPresent(optional -> {
                    try {
                        LOGGER.info("Start plugin: {}", optional.getType());
                        optional.start(entry.getValue(), isEnhancedForProxy);
                        // CHECKSTYLE:OFF
                    } catch (final Throwable ex) {
                        // CHECKSTYLE:ON
                        LOGGER.error("Failed to start service.", ex);
                    }
                });
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    /**
     * Close all services.
     */
    public static void closeAllServices() {
        AgentSPIRegistry.getAllRegisteredServices(PluginBootService.class).forEach(each -> {
            try {
                each.close();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to close service.", ex);
            }
        });
        PluginJarHolder.getPluginJars().forEach(each -> {
            try {
                each.getJarFile().close();
            } catch (final IOException ex) {
                LOGGER.error("Failed to close jar file.", ex);
            }
        });
    }
}
