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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.spi.AgentTypedSPIRegistry;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Plugin boot service manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PluginBootServiceManager {
    
    /**
     * Start all services.
     *
     * @param pluginConfigurationMap plugin configuration map
     */
    public static void startAllServices(final Map<String, PluginConfiguration> pluginConfigurationMap) {
        for (Entry<String, PluginConfiguration> entry : pluginConfigurationMap.entrySet()) {
            AgentTypedSPIRegistry.getRegisteredServiceOptional(PluginBootService.class, entry.getKey()).ifPresent(optional -> {
                try {
                    log.info("Start plugin: {}", optional.getType());
                    optional.start(entry.getValue());
                    // CHECKSTYLE:OFF
                } catch (final Throwable ex) {
                    // CHECKSTYLE:ON
                    log.error("Failed to start service", ex);
                }
            });
        }
    }
    
    /**
     * Close all services.
     */
    public static void closeAllServices() {
        AgentTypedSPIRegistry.getAllRegisteredService(PluginBootService.class).forEach(each -> {
            try {
                each.close();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to close service", ex);
            }
        });
    }
}
