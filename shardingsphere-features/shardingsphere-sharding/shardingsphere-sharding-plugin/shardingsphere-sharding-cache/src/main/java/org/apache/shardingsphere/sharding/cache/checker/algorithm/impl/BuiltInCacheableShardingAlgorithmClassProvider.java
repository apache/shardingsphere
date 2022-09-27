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

package org.apache.shardingsphere.sharding.cache.checker.algorithm.impl;

import org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm;
import org.apache.shardingsphere.sharding.cache.checker.algorithm.CacheableShardingAlgorithmClassProvider;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Arrays;
import java.util.Collection;

/**
 * Built-in cacheable sharding algorithm class provider.
 */
public final class BuiltInCacheableShardingAlgorithmClassProvider implements CacheableShardingAlgorithmClassProvider {
    
    @Override
    public Collection<Class<? extends ShardingAlgorithm>> getCacheableShardingAlgorithmClasses() {
        return Arrays.asList(ModShardingAlgorithm.class, HashModShardingAlgorithm.class, VolumeBasedRangeShardingAlgorithm.class, BoundaryBasedRangeShardingAlgorithm.class);
    }
}
