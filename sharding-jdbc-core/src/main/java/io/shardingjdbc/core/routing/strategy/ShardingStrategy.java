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

package io.shardingjdbc.core.routing.strategy;

import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;

import java.util.Collection;

/**
 * Sharding strategy.
 * 
 * @author zhangliang
 */
public interface ShardingStrategy {
    
    /**
     * Get sharding columns.
     * 
     * @return sharding columns
     */
    Collection<String> getShardingColumns();
    
    /**
     * Sharding.
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValues sharding values
     * @return sharding results for data sources or tables's names
     */
    Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue> shardingValues);
}
