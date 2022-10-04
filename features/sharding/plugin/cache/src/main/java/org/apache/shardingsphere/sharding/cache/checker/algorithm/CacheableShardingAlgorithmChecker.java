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

package org.apache.shardingsphere.sharding.cache.checker.algorithm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.HashSet;

/**
 * Cacheable sharding algorithm checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheableShardingAlgorithmChecker {
    
    static {
        Collection<Class<? extends ShardingAlgorithm>> result = new HashSet<>();
        ShardingSphereServiceLoader.register(CacheableShardingAlgorithmClassProvider.class);
        for (CacheableShardingAlgorithmClassProvider each : ShardingSphereServiceLoader.getServiceInstances(CacheableShardingAlgorithmClassProvider.class)) {
            result.addAll(each.getCacheableShardingAlgorithmClasses());
        }
        CACHEABLE_SHARDING_ALGORITHM_CLASSES = result;
    }
    
    private static final Collection<Class<? extends ShardingAlgorithm>> CACHEABLE_SHARDING_ALGORITHM_CLASSES;
    
    /**
     * Check if sharding algorithm is cacheable.
     *
     * @param shardingAlgorithm instance of sharding algorithm
     * @return is sharding algorithm cacheable
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isCacheableShardingAlgorithm(final ShardingAlgorithm shardingAlgorithm) {
        return CACHEABLE_SHARDING_ALGORITHM_CLASSES.contains(shardingAlgorithm.getClass());
    }
}
