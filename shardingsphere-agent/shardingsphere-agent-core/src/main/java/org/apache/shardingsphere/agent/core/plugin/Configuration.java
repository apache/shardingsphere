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
 * Configuration of plugin.
 */
public abstract class Configuration {
    
    private final Map<String, PluginAdviceDefine.Builder> defineMap = Maps.newHashMap();
    
    private final List<Service> services = Lists.newArrayList();
    
    /**
     * Configure the plugin here.
     */
    protected abstract void configure();
    
    protected PluginAdviceDefine.Builder intercept(final String classNameOfTarget) {
        if (defineMap.containsKey(classNameOfTarget)) {
            return defineMap.get(classNameOfTarget);
        }
        PluginAdviceDefine.Builder builder = PluginAdviceDefine.intercept(classNameOfTarget);
        defineMap.put(classNameOfTarget, builder);
        return builder;
    }
    
    protected void registerService(final Service service) {
        services.add(service);
    }
    
    /**
     * To build Plugin definition.
     *
     * @return configurations.
     */
    public final List<PluginAdviceDefine> build() {
        configure();
        return defineMap.values().stream()
                .map(PluginAdviceDefine.Builder::install)
                .collect(Collectors.toList());
    }
    
    /**
     * To get all services.
     *
     * @return all services.
     */
    public List<Service> getAllServics() {
        return services;
    }
}
