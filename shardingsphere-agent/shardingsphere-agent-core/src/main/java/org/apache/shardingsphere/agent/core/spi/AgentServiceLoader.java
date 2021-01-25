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

package org.apache.shardingsphere.agent.core.spi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.shardingsphere.agent.core.plugin.PluginLoader;

/**
 * Agent service loader.
 */
public final class AgentServiceLoader<T> {
    
    private static final Map<Class<?>, AgentServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();
    
    private final Map<Class<?>, Collection<T>> serviceMap = new ConcurrentHashMap<>();
    
    private final Class<T> service;

    private AgentServiceLoader(final Class<T> service) {
        this.service = service;
        register(service);
    }
    
    /**
     * Get singleton agent service loader.
     *
     * @param service service type
     * @param <T> type of class
     * @return agent service loader
     */
    @SuppressWarnings("unchecked")
    public static <T> AgentServiceLoader<T> getServiceLoader(final Class<T> service) {
        if (null == service) {
            throw new NullPointerException("extension clazz is null");
        }
        if (!service.isInterface()) {
            throw new IllegalArgumentException(String.format("extension clazz ( %s is not interface!", service));
        }
        AgentServiceLoader<T> agentServiceLoader = (AgentServiceLoader<T>) LOADERS.get(service);
        if (null != agentServiceLoader) {
            return agentServiceLoader;
        }
        LOADERS.putIfAbsent(service, new AgentServiceLoader<>(service));
        return (AgentServiceLoader<T>) LOADERS.get(service);
    }
    
    /**
     * New service instances.
     *
     * @return service instances
     */
    public Collection<T> newServiceInstances() {
        return serviceMap.get(service);
    }
    
    private void register(final Class<T> service) {
        if (serviceMap.containsKey(service)) {
            return;
        }
        serviceMap.put(service, new LinkedList<>());
        for (T each : ServiceLoader.load(service, PluginLoader.getInstance())) {
            serviceMap.get(service).add(each);
        }
    }
}
