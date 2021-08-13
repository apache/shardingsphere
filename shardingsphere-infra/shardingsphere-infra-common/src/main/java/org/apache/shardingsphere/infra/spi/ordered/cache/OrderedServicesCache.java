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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Ordered services cached.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderedServicesCache {
    
    private static final Cache<OrderedServicesCacheKey, Map<?, ?>> CACHED_SERVICES = CacheBuilder.newBuilder().softValues().build();
    
    /**
     * Find cached services.
     * 
     * @param orderedSPIClass class of ordered SPI
     * @param types types
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @return cached ordered services
     */
    @SuppressWarnings("unchecked")
    public static <K, V extends OrderedSPI<?>> Optional<Map<K, V>> findCachedServices(final Class<V> orderedSPIClass, final Collection<K> types) {
        return Optional.ofNullable(CACHED_SERVICES.getIfPresent(new OrderedServicesCacheKey(orderedSPIClass, types))).map(optional -> (Map<K, V>) optional);
    }
    
    /**
     * Cache services.
     * 
     * @param orderedSPIClass class of ordered SPI
     * @param types types
     * @param services ordered services
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     */
    public static <K, V extends OrderedSPI<?>> void cacheServices(final Class<V> orderedSPIClass, final Collection<K> types, final Map<K, V> services) {
        CACHED_SERVICES.put(new OrderedServicesCacheKey(orderedSPIClass, types), services);
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static final class OrderedServicesCacheKey {
    
        private final Class<?> clazz;
    
        private final Collection<?> types;
    }
}
