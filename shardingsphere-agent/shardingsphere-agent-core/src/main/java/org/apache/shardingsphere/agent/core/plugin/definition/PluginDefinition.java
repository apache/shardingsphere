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

package org.apache.shardingsphere.agent.core.plugin.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.core.plugin.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.plugin.service.BootService;

/**
 * Plugin definition.
 */
@RequiredArgsConstructor
public abstract class PluginDefinition {
    
    private final Map<String, PluginInterceptorPoint.Builder> interceptorPointMap = Maps.newHashMap();
    
    private final List<Class<? extends BootService>> bootServices = Lists.newArrayList();
    
    @Getter
    private final String pluginName;
    
    protected abstract void define();
    
    protected PluginInterceptorPoint.Builder intercept(final String classNameOfTarget) {
        if (interceptorPointMap.containsKey(classNameOfTarget)) {
            return interceptorPointMap.get(classNameOfTarget);
        }
        PluginInterceptorPoint.Builder builder = PluginInterceptorPoint.intercept(classNameOfTarget);
        interceptorPointMap.put(classNameOfTarget, builder);
        return builder;
    }
    
    /**
     * Register boot service to agent.
     *
     * @param service the class of Service
     */
    protected void registerService(final Class<? extends BootService> service) {
        bootServices.add(service);
    }
    
    /**
     * Build collection of pluginInterceptorPoint.
     *
     * @return Collection of pluginInterceptorPoint
     */
    public final List<PluginInterceptorPoint> build() {
        define();
        return interceptorPointMap.values().stream().map(PluginInterceptorPoint.Builder::install).collect(Collectors.toList());
    }
    
    /**
     * To get all boot services.
     *
     * @return all boot services
     */
    public List<Class<? extends BootService>> getAllServices() {
        return bootServices;
    }
}
