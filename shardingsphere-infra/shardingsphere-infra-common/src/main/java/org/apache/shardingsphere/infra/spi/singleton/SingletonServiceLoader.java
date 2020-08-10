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

package org.apache.shardingsphere.infra.spi.singleton;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton service loader.
 */
public final class SingletonServiceLoader<T> {
    
    private static final Map<Class<?>, SingletonServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();
    
    private final Map<Class<?>, Collection<T>> serviceMap = new ConcurrentHashMap<>();
    
    private final Class<T> service;

    private SingletonServiceLoader(final Class<T> service) {
        this.service = service;
        register(service);
    }
    
    /**
     * Get singleton service loader.
     *
     * @param service service type
     * @param <T> type of service
     * @return singleton service loader.
     */
    @SuppressWarnings("unchecked")
    public static <T> SingletonServiceLoader<T> getServiceLoader(final Class<T> service) {
        if (null == service) {
            throw new NullPointerException("extension clazz is null");
        }
        if (!service.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + service + "is not interface!");
        }
        SingletonServiceLoader<T> serviceLoader = (SingletonServiceLoader<T>) LOADERS.get(service);
        if (null != serviceLoader) {
            return serviceLoader;
        }
        LOADERS.putIfAbsent(service, new SingletonServiceLoader<>(service));
        return (SingletonServiceLoader<T>) LOADERS.get(service);
    }
    
    /**
     * New service instances.
     *
     * @return service instances
     */
    public Optional<T> newServiceInstances() {
        return serviceMap.get(service).stream().findFirst();
    }
    
    private void register(final Class<T> service) {
        if (serviceMap.containsKey(service)) {
            return;
        }
        serviceMap.put(service, new LinkedList<>());
        for (T each : ServiceLoader.load(service)) {
            serviceMap.get(service).add(each);
        }
    }
}
