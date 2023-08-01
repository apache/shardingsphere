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

package org.apache.shardingsphere.infra.spi.type.ordered;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Ordered SPI loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderedSPILoader {
    
    /**
     * Get services by class type.
     *
     * @param serviceInterface ordered SPI service interface
     * @param types types
     * @param <T> type of ordered SPI class
     * @return got services
     */
    public static <T extends OrderedSPI<?>> Map<Class<?>, T> getServicesByClass(final Class<T> serviceInterface, final Collection<Class<?>> types) {
        Collection<T> services = getServices(serviceInterface);
        Map<Class<?>, T> result = new LinkedHashMap<>(services.size(), 1F);
        for (T each : services) {
            types.stream().filter(type -> each.getTypeClass() == type).forEach(type -> result.put(type, each));
        }
        return result;
    }
    
    /**
     * Get services.
     *
     * @param serviceInterface ordered SPI service interface
     * @param types types
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @return got services
     */
    public static <K, V extends OrderedSPI<?>> Map<K, V> getServices(final Class<V> serviceInterface, final Collection<K> types) {
        return getServices(serviceInterface, types, Comparator.naturalOrder());
    }
    
    /**
     * Get services.
     *
     * @param serviceInterface ordered SPI service interface
     * @param types types
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @param orderComparator order comparator
     * @return got services
     */
    @SuppressWarnings("unchecked")
    public static <K, V extends OrderedSPI<?>> Map<K, V> getServices(final Class<V> serviceInterface, final Collection<K> types, final Comparator<Integer> orderComparator) {
        Optional<Map<K, V>> cachedServices = OrderedServicesCache.findCachedServices(serviceInterface, types).map(optional -> (Map<K, V>) optional);
        if (cachedServices.isPresent()) {
            return cachedServices.get();
        }
        Collection<V> services = getServices(serviceInterface, orderComparator);
        Map<K, V> result = new LinkedHashMap<>(services.size(), 1F);
        for (V each : services) {
            types.stream().filter(type -> each.getTypeClass() == type.getClass()).forEach(type -> result.put(type, each));
        }
        OrderedServicesCache.cacheServices(serviceInterface, types, result);
        return result;
    }
    
    /**
     * Get services.
     *
     * @param serviceInterface ordered SPI service interface
     * @param <T> type of ordered SPI class
     * @return got services
     */
    public static <T extends OrderedSPI<?>> Collection<T> getServices(final Class<T> serviceInterface) {
        return getServices(serviceInterface, Comparator.naturalOrder());
    }
    
    private static <T extends OrderedSPI<?>> Collection<T> getServices(final Class<T> serviceInterface, final Comparator<Integer> comparator) {
        Map<Integer, T> result = new TreeMap<>(comparator);
        for (T each : ShardingSphereServiceLoader.getServiceInstances(serviceInterface)) {
            Preconditions.checkArgument(!result.containsKey(each.getOrder()), "Found same order `%s` with `%s` and `%s`", each.getOrder(), result.get(each.getOrder()), each);
            result.put(each.getOrder(), each);
        }
        return result.values();
    }
}
