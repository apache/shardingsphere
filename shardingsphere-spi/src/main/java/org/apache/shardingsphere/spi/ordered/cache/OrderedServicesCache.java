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

package org.apache.shardingsphere.spi.ordered.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Ordered services cached.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderedServicesCache {
    
    private static final Cache<Key, Map<?, ?>> CACHED_SERVICES = CacheBuilder.newBuilder().softValues().build();
    
    /**
     * Find cached services.
     * 
     * @param spiClass SPI class
     * @param types types
     * @return cached services
     */
    public static Optional<Map<?, ?>> findCachedServices(final Class<?> spiClass, final Collection<?> types) {
        return Optional.ofNullable(CACHED_SERVICES.getIfPresent(new Key(spiClass, types)));
    }
    
    /**
     * Cache services.
     * 
     * @param spiClass SPI class
     * @param types types
     * @param services services to be cached
     */
    public static void cacheServices(final Class<?> spiClass, final Collection<?> types, final Map<?, ?> services) {
        CACHED_SERVICES.put(new Key(spiClass, types), services);
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static final class Key {
        
        private final Class<?> clazz;
        
        private final Collection<?> types;
    }
}
