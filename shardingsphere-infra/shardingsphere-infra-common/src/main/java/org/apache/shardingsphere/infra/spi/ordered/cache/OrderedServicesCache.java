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

package org.apache.shardingsphere.infra.spi.ordered.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ordered services cached.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderedServicesCache {
    
    private static final Map<Class<?>, CachedOrderedServices> CACHED_SERVICES = new ConcurrentHashMap<>();
    
    /**
     * Find cached services.
     * 
     * @param types types
     * @param orderedSPIClass class of ordered SPI
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @return cached ordered services
     */
    public static <K, V extends OrderedSPI<?>> Optional<CachedOrderedServices> findCachedServices(final Collection<K> types, final Class<V> orderedSPIClass) {
        return isHitCache(types, orderedSPIClass) ? Optional.of(CACHED_SERVICES.get(orderedSPIClass)) : Optional.empty();
    }
    
    private static <K, V extends OrderedSPI<?>> boolean isHitCache(final Collection<K> types, final Class<V> orderedSPIClass) {
        return CACHED_SERVICES.containsKey(orderedSPIClass) && CACHED_SERVICES.get(orderedSPIClass).getTypes().equals(types);
    }
    
    /**
     * Cache services.
     * 
     * @param types types
     * @param orderedSPIClass class of ordered SPI
     * @param services ordered services
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     */
    public static <K, V extends OrderedSPI<?>> void cacheServices(final Collection<K> types, final Class<V> orderedSPIClass, final Map<K, V> services) {
        CACHED_SERVICES.put(orderedSPIClass, new CachedOrderedServices(types, services));
    }
}
