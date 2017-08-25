/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.routing.strategy;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;

import java.util.Collection;

/**
 * None key sharding algorithm.
 * 
 * @param <T> class type for sharding value 
 * @author gaohongtao
 */
public interface NoneKeyShardingAlgorithm<T extends Comparable<?>> extends ShardingAlgorithm {
    
    /**
     * Calculate and do sharding.
     * 
     * <p>sharding value injected by hint, not in SQL.</p>
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding values
     * @return sharding results for data sources or tables's names
     */
    String doSharding(Collection<String> availableTargetNames, ShardingValue<T> shardingValue);
}
