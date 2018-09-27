/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.strategy.hint;

import com.google.common.base.Preconditions;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.api.algorithm.sharding.hint.HintShardingAlgorithm;
import io.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingsphere.core.routing.strategy.ShardingStrategy;
import lombok.Getter;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Hint sharding strategy.
 * 
 * @author zhangliang
 */
public final class HintShardingStrategy implements ShardingStrategy {
    
    @Getter
    private final Collection<String> shardingColumns;
    
    private final HintShardingAlgorithm shardingAlgorithm;
    
    public HintShardingStrategy(final HintShardingStrategyConfiguration hintShardingStrategyConfig) {
        Preconditions.checkNotNull(hintShardingStrategyConfig.getShardingAlgorithm(), "Sharding algorithm cannot be null.");
        shardingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        shardingAlgorithm = hintShardingStrategyConfig.getShardingAlgorithm();
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue> shardingValues) {
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames, shardingValues.iterator().next());
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
}
