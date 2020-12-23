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

package org.apache.shardingsphere.agent.core.plugin.service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.core.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.spi.AgentTypedSPIRegistry;

/**
 * Service supervisor.
 */
@Slf4j
@SuppressWarnings("ALL")
public final class ServiceSupervisor {
    
    /**
     * Set up all service.
     *
     * @param pluginConfigurations plugin configurations
     */
    public static void setupAllService(final Collection<PluginConfiguration> pluginConfigurations) {
        Collection<String> pluginNames = pluginConfigurations.stream().map(PluginConfiguration::getPluginName).collect(Collectors.toList());
        for (Map.Entry<String, BootService> entry : AgentTypedSPIRegistry.getRegisteredServices(pluginNames, BootService.class).entrySet()) {
            for (PluginConfiguration each : pluginConfigurations) {
                if (each.getPluginName().equals(entry.getKey())) {
                    try {
                        entry.getValue().setup(each);
                        // CHECKSTYLE:OFF
                    } catch (final Throwable ex) {
                        // CHECKSTYLE:ON
                        log.error("Failed to setup service.", ex);
                    }
                }
            }
        }
    }
    
    /**
     * Start all service.
     *
     * @param pluginConfigurations plugin configurations
     */
    public static void startAllService(final Collection<PluginConfiguration> pluginConfigurations) {
        Collection<String> pluginNames = pluginConfigurations.stream().map(PluginConfiguration::getPluginName).collect(Collectors.toList());
        for (Map.Entry<String, BootService> entry : AgentTypedSPIRegistry.getRegisteredServices(pluginNames, BootService.class).entrySet()) {
            for (PluginConfiguration each : pluginConfigurations) {
                if (each.getPluginName().equals(entry.getKey())) {
                    try {
                        entry.getValue().start(each);
                        // CHECKSTYLE:OFF
                    } catch (final Throwable ex) {
                        // CHECKSTYLE:ON
                        log.error("Failed to start service.", ex);
                    }
                }
            }
        }
    }
    
    /**
     * Clern all service.
     */
    public static void clernAllService() {
        AgentTypedSPIRegistry.getAllRegisteredService(BootService.class).forEach(each -> {
            try {
                each.cleanup();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to shutdown service.", ex);
            }
        });
    }
}
