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

package org.apache.shardingsphere.infra.util.spi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere service loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereServiceLoader {
    
    private static final Map<Class<?>, Collection<Object>> SERVICES = new ConcurrentHashMap<>();
    
    /**
     * Register service.
     *
     * @param serviceInterface service interface
     */
    public static void register(final Class<?> serviceInterface) {
        if (!SERVICES.containsKey(serviceInterface)) {
            SERVICES.put(serviceInterface, load(serviceInterface));
        }
    }
    
    private static <T> Collection<Object> load(final Class<T> serviceInterface) {
        Collection<Object> result = new LinkedList<>();
        for (T each : ServiceLoader.load(serviceInterface)) {
            result.add(each);
        }
        return result;
    }
    
    /**
     * Get service instances.
     * 
     * @param serviceInterface service interface
     * @param <T> type of service
     * @return service instances
     */
    public static <T> Collection<T> getServiceInstances(final Class<T> serviceInterface) {
        return null == serviceInterface.getAnnotation(SingletonSPI.class) ? createNewServiceInstances(serviceInterface) : getSingletonServiceInstances(serviceInterface);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> createNewServiceInstances(final Class<T> serviceInterface) {
        if (!SERVICES.containsKey(serviceInterface)) {
            return Collections.emptyList();
        }
        Collection<Object> services = SERVICES.get(serviceInterface);
        if (services.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<T> result = new LinkedList<>();
        for (Object each : services) {
            result.add((T) each.getClass().getDeclaredConstructor().newInstance());
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> getSingletonServiceInstances(final Class<T> serviceInterface) {
        return (Collection<T>) SERVICES.getOrDefault(serviceInterface, Collections.emptyList());
    }
}
