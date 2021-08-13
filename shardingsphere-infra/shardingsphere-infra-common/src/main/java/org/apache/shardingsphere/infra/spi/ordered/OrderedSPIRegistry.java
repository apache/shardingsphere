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

package org.apache.shardingsphere.infra.spi.ordered;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.infra.spi.ordered.cache.OrderedServicesCacheKey;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Ordered SPI registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderedSPIRegistry {
    
    /**
     * Get registered services by class type.
     *
     * @param types types
     * @param orderedSPIClass class of ordered SPI
     * @param <T> type of ordered SPI class
     * @return registered services
     */
    public static <T extends OrderedSPI<?>> Map<Class<?>, T> getRegisteredServicesByClass(final Collection<Class<?>> types, final Class<T> orderedSPIClass) {
        Collection<T> registeredServices = getRegisteredServices(orderedSPIClass);
        Map<Class<?>, T> result = new LinkedHashMap<>(registeredServices.size(), 1);
        for (T each : registeredServices) {
            types.stream().filter(type -> each.getTypeClass() == type).forEach(type -> result.put(type, each));
        }
        return result;
    }
    
    /**
     * Get registered services.
     *
     * @param types types
     * @param orderedSPIClass class of ordered SPI
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @return registered services
     */
    public static <K, V extends OrderedSPI<?>> Map<K, V> getRegisteredServices(final Collection<K> types, final Class<V> orderedSPIClass) {
        return getRegisteredServices(types, orderedSPIClass, Comparator.naturalOrder());
    }
    
    /**
     * Get registered services.
     *
     * @param types types
     * @param orderedSPIClass class of ordered SPI
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @param comparator comparator
     * @return registered services
     */
    public static <K, V extends OrderedSPI<?>> Map<K, V> getRegisteredServices(final Collection<K> types, final Class<V> orderedSPIClass, final Comparator<Integer> comparator) {
        OrderedServicesCacheKey cacheKey = new OrderedServicesCacheKey(orderedSPIClass, types);
        Optional<Map<K, V>> cachedServices = OrderedServicesCache.findCachedServices(cacheKey);
        if (cachedServices.isPresent()) {
            return cachedServices.get();
        }
        Collection<V> registeredServices = getRegisteredServices(orderedSPIClass, comparator);
        Map<K, V> result = new LinkedHashMap<>(registeredServices.size(), 1);
        for (V each : registeredServices) {
            types.stream().filter(type -> each.getTypeClass() == type.getClass()).forEach(type -> result.put(type, each));
        }
        OrderedServicesCache.cacheServices(cacheKey, result);
        return result;
    }
    
    /**
     * Get registered services.
     *
     * @param orderedSPIClass class of ordered SPI
     * @param <T> type of ordered SPI class
     * @return registered services
     */
    public static <T extends OrderedSPI<?>> Collection<T> getRegisteredServices(final Class<T> orderedSPIClass) {
        return getRegisteredServices(orderedSPIClass, Comparator.naturalOrder());
    }
    
    /**
     * Get registered services.
     *
     * @param orderedSPIClass class of ordered SPI
     * @param <T> type of ordered SPI class
     * @param comparator comparator
     * @return registered services
     */
    public static <T extends OrderedSPI<?>> Collection<T> getRegisteredServices(final Class<T> orderedSPIClass, final Comparator<Integer> comparator) {
        Map<Integer, T> result = new TreeMap<>(comparator);
        for (T each : ShardingSphereServiceLoader.getSingletonServiceInstances(orderedSPIClass)) {
            Preconditions.checkArgument(!result.containsKey(each.getOrder()), "Found same order `%s` with `%s` and `%s`", each.getOrder(), result.get(each.getOrder()), each);
            result.put(each.getOrder(), each);
        }
        return result.values();
    }
}
