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

package io.shardingjdbc.core.routing.strategy.standard;

import io.shardingjdbc.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.sharding.standard.RangeShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Standard sharding strategy.
 * 
 * @author zhangliang
 */
public final class StandardShardingStrategy implements ShardingStrategy {
    
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
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue> shardingValues) {
        ShardingValue shardingValue = shardingValues.iterator().next();
        Collection<String> shardingResult = shardingValue instanceof ListShardingValue
                ? doSharding(availableTargetNames, (ListShardingValue) shardingValue) : doSharding(availableTargetNames, (RangeShardingValue) shardingValue);
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<?> shardingValue) {
        if (!rangeShardingAlgorithm.isPresent()) {
            throw new UnsupportedOperationException("Cannot find range sharding strategy in sharding rule.");
        }
        return rangeShardingAlgorithm.get().doSharding(availableTargetNames, shardingValue);
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final ListShardingValue<?> shardingValue) {
        Collection<String> result = new LinkedList<>();
        for (PreciseShardingValue<?> each : transferToPreciseShardingValues(shardingValue)) {
            result.add(preciseShardingAlgorithm.doSharding(availableTargetNames, each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<PreciseShardingValue> transferToPreciseShardingValues(final ListShardingValue<?> shardingValue) {
        List<PreciseShardingValue> result = new ArrayList<>(shardingValue.getValues().size());
        for (Comparable<?> each : shardingValue.getValues()) {
            result.add(new PreciseShardingValue(shardingValue.getLogicTableName(), shardingValue.getColumnName(), each));
        }
        return result;
    }
    
    @Override
    public Collection<String> getShardingColumns() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(shardingColumn);
        return result;
    }
}
