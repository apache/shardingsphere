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
 * Single key sharding algorithm.
 * 
 * @author zhangliang
 * 
 * @param <T> class type of sharding value
 */
public interface SingleKeyShardingAlgorithm<T extends Comparable<?>> extends ShardingAlgorithm {
    
    /**
     * Sharding with equal operator.
     * 
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding value
     * @return sharding results for data sources or tables's names
     */
    String doEqualSharding(Collection<String> availableTargetNames, ShardingValue<T> shardingValue);
    
    /**
     * Sharding with in operator.
     * 
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding value
     * @return sharding results for data sources or tables's names
     */
    Collection<String> doInSharding(Collection<String> availableTargetNames, ShardingValue<T> shardingValue);
    
    /**
     * Sharding with between operator.
     * 
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding value
     * @return sharding results for data sources or tables's names
     */
    Collection<String> doBetweenSharding(Collection<String> availableTargetNames, ShardingValue<T> shardingValue);
}
