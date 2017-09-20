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

package io.shardingjdbc.core.routing.strategy.complex;

import io.shardingjdbc.core.api.algorithm.sharding.complex.ComplexKeysShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import lombok.Getter;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Complex sharding strategy.
 * 
 * @author zhangliang
 */
public final class ComplexShardingStrategy implements ShardingStrategy {
    
    @Getter
    private final Collection<String> shardingColumns;
    
    private final ComplexKeysShardingAlgorithm shardingAlgorithm;
    
    public ComplexShardingStrategy(final Collection<String> shardingColumns, final ComplexKeysShardingAlgorithm shardingAlgorithm) {
        this.shardingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.shardingColumns.addAll(shardingColumns);
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue> shardingValues) {
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames, shardingValues);
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
}
