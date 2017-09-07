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

package com.dangdang.ddframe.rdb.sharding.routing.strategy.standard;

import com.dangdang.ddframe.rdb.sharding.api.PreciseShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.RangeShardingValue;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Standard sharding strategy.
 * 
 * @author zhangliang
 */
@Getter
public final class StandardShardingStrategy extends ShardingStrategy {
    
    private final String shardingColumn;
    
    private final PreciseShardingAlgorithm preciseShardingAlgorithm;
    
    private final Optional<RangeShardingAlgorithm> rangeShardingAlgorithm;
    
    public StandardShardingStrategy(final String shardingColumn, final PreciseShardingAlgorithm preciseShardingAlgorithm) {
        this(shardingColumn, preciseShardingAlgorithm, null);
    }
    
    public StandardShardingStrategy(final String shardingColumn, final PreciseShardingAlgorithm preciseShardingAlgorithm, final RangeShardingAlgorithm rangeShardingAlgorithm) {
        this.shardingColumn = shardingColumn;
        this.preciseShardingAlgorithm = preciseShardingAlgorithm;
        this.rangeShardingAlgorithm = Optional.fromNullable(rangeShardingAlgorithm);
    }
    
    /**
     * Calculate precise sharding info.
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding value
     * @return sharding results for data sources or tables's names
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String doPreciseSharding(final Collection<String> availableTargetNames, final PreciseShardingValue shardingValue) {
        return preciseShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }
    
    /**
     * Calculate range sharding info.
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding value
     * @return sharding results for data sources or tables's names
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<String> doRangeSharding(final Collection<String> availableTargetNames, final RangeShardingValue shardingValue) {
        if (!rangeShardingAlgorithm.isPresent()) {
            throw new UnsupportedOperationException("Cannot find range sharding strategy in sharding rule.");
        }
        return rangeShardingAlgorithm.get().doSharding(availableTargetNames, shardingValue);
    }
    
    @Override
    public Collection<String> getShardingColumns() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(shardingColumn);
        return result;
    }
}
