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
import com.dangdang.ddframe.rdb.sharding.routing.strategy.complex.ComplexKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.hint.HintShardingAlgorithm;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * Sharding strategy.
 * 
 * @author zhangliang
 */
@NoArgsConstructor
public class ShardingStrategy {
    
    @Getter
    private Collection<String> shardingColumns;
    
    private ShardingAlgorithm shardingAlgorithm;
    
    public ShardingStrategy(final Collection<String> shardingColumns, final ShardingAlgorithm shardingAlgorithm) {
        this.shardingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.shardingColumns.addAll(shardingColumns);
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    /**
     * Calculate static sharding info.
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValues sharding values
     * @return sharding results for data sources or tables's names
     */
    public Collection<String> doStaticSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue> shardingValues) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (shardingValues.isEmpty()) {
            result.addAll(availableTargetNames);
        } else {
            result.addAll(doSharding(shardingValues, availableTargetNames));
        }
        return result;
    }
    
    /**
     * Calculate dynamic sharding info.
     *
     * @param shardingValues sharding values
     * @return sharding results for data sources or tables's names
     */
    public Collection<String> doDynamicSharding(final Collection<ShardingValue> shardingValues) {
        Preconditions.checkState(!shardingValues.isEmpty(), "Dynamic table should contain sharding value.");
        Collection<String> availableTargetNames = Collections.emptyList();
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(doSharding(shardingValues, availableTargetNames));
        return result;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Collection<String> doSharding(final Collection<ShardingValue> shardingValues, final Collection<String> availableTargetNames) {
        if (shardingAlgorithm instanceof HintShardingAlgorithm) {
            return ((HintShardingAlgorithm) shardingAlgorithm).doSharding(availableTargetNames, shardingValues.iterator().next());
        }
        if (shardingAlgorithm instanceof ComplexKeysShardingAlgorithm) {
            return ((ComplexKeysShardingAlgorithm) shardingAlgorithm).doSharding(availableTargetNames, shardingValues);
        }
        throw new UnsupportedOperationException(shardingAlgorithm.getClass().getName());
    }
}
