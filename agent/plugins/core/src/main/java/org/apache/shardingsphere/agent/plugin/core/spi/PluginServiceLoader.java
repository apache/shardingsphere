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

package org.apache.shardingsphere.agent.plugin.core.spi;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin service loader.
 * 
 * @param <T> type of service
 */
public final class PluginServiceLoader<T> {
    
    private static final Map<Class<?>, PluginServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();
    
    private final Collection<T> services;
    
    private PluginServiceLoader(final Class<T> service) {
        validate(service);
        this.services = load(service);
    }
    
    private void validate(final Class<T> service) {
        Preconditions.checkNotNull(service, "SPI class is null.");
        Preconditions.checkArgument(service.isInterface(), "SPI class `%s` is not interface.", service);
    }
    
    private Collection<T> load(final Class<T> service) {
        Collection<T> result = new LinkedList<>();
        for (T each : ServiceLoader.load(service)) {
            result.add(each);
        }
        return result;
    }
    
    /**
     * Get singleton agent service loader.
     *
     * @param service service type
     * @param <T> type of class
     * @return agent service loader
     */
    @SuppressWarnings("unchecked")
    public static <T extends PluginTypedSPI> PluginServiceLoader<T> getServiceLoader(final Class<T> service) {
        return (PluginServiceLoader<T>) LOADERS.computeIfAbsent(service, PluginServiceLoader::new);
    }
    
    /**
     * Get service.
     * 
     * @param pluginType plugin type
     * @return service
     */
    public T getService(final String pluginType) {
        return services.stream().filter(each -> pluginType.equalsIgnoreCase(((PluginTypedSPI) each).getType())).findFirst().orElseThrow(() -> new UnsupportedOperationException(pluginType));
    }
}
