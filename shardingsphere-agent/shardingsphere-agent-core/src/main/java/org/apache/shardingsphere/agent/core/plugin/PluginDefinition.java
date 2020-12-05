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
 *
 */

package org.apache.shardingsphere.agent.core.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin definition.
 */
public abstract class PluginDefinition {
    
    private final Map<String, PluginAdviceDefinition.Builder> defineMap = Maps.newHashMap();
    
    private final List<Class<? extends Service>> services = Lists.newArrayList();

    private final String pluginName;

    public PluginDefinition(final String pluginName) {
        this.pluginName = pluginName;
    }
    
    /**
     * Define the plugin here.
     */
    protected abstract void define();
    
    protected PluginAdviceDefinition.Builder intercept(final String classNameOfTarget) {
        if (defineMap.containsKey(classNameOfTarget)) {
            return defineMap.get(classNameOfTarget);
        }
        PluginAdviceDefinition.Builder builder = PluginAdviceDefinition.intercept(classNameOfTarget);
        defineMap.put(classNameOfTarget, builder);
        return builder;
    }
    
    /**
     * Register service to agent.
     *
     * @param service the class of Service
     */
    protected void registerService(final Class<? extends Service> service) {
        services.add(service);
    }
    
    /**
     * To build Plugin definition.
     *
     * @return configurations
     */
    public final List<PluginAdviceDefinition> build() {
        define();
        return defineMap.values().stream().map(PluginAdviceDefinition.Builder::install).collect(Collectors.toList());
    }
    
    /**
     * To get all services.
     *
     * @return all services
     */
    public List<Class<? extends Service>> getAllServices() {
        return services;
    }

    /**
     * To get plugin name.
     *
     * @return plugin name
     */
    public String getPluginName() {
        return pluginName;
    }
}
