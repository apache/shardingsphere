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

package org.apache.shardingsphere.sharding.cache.route.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheOptions;

import java.util.Optional;

/**
 * Cache for sharding route.
 */
public final class ShardingRouteCache {
    
    private final Cache<ShardingRouteCacheKey, ShardingRouteCacheValue> cache;
    
    public ShardingRouteCache(final ShardingCacheOptions cacheOptions) {
        cache = buildRouteCache(cacheOptions);
    }
    
    private Cache<ShardingRouteCacheKey, ShardingRouteCacheValue> buildRouteCache(final ShardingCacheOptions cacheOptions) {
        Caffeine<Object, Object> result = Caffeine.newBuilder().initialCapacity(cacheOptions.getInitialCapacity()).maximumSize(cacheOptions.getMaximumSize());
        if (cacheOptions.isSoftValues()) {
            result.softValues();
        }
        return result.build();
    }
    
    /**
     * Cache route result.
     *
     * @param key cache key
     * @param value cache value
     */
    public void put(final ShardingRouteCacheKey key, final ShardingRouteCacheValue value) {
        cache.put(key, value);
    }
    
    /**
     * Get cached route result.
     *
     * @param key cache key
     * @return optional cached route result
     */
    public Optional<ShardingRouteCacheValue> get(final ShardingRouteCacheKey key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }
}
