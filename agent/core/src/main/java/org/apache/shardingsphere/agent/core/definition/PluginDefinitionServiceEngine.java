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

package org.apache.shardingsphere.agent.core.definition;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.pointcut.ClassPointcuts;
import org.apache.shardingsphere.agent.spi.PluginDefinitionService;

import java.util.Collection;

/**
 * Plugin definition service engine.
 */
@RequiredArgsConstructor
public final class PluginDefinitionServiceEngine {
    
    private final PluginDefinitionService pluginDefinitionService;
    
    /**
     * Get class pointcuts.
     *
     * @param targetClassName target class name
     * @return class pointcuts
     */
    public ClassPointcuts getClassPointcuts(final String targetClassName) {
        return ClassPointcutsRegistryFactory.getRegistry(pluginDefinitionService.getType()).getClassPointcuts(targetClassName);
    }
    
    /**
     * Install plugins.
     * 
     * @param isEnhancedForProxy is enhanced for proxy
     * @return class pointcuts
     */
    public Collection<ClassPointcuts> install(final boolean isEnhancedForProxy) {
        if (isEnhancedForProxy) {
            pluginDefinitionService.installProxyInterceptors();
        } else {
            pluginDefinitionService.installJdbcInterceptors();
        }
        return ClassPointcutsRegistryFactory.getRegistry(pluginDefinitionService.getType()).getAllClassPointcuts();
    }
}
