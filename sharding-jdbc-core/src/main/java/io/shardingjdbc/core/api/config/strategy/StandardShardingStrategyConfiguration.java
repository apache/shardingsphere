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

package io.shardingjdbc.core.api.config.strategy;

import io.shardingjdbc.core.routing.strategy.ShardingAlgorithmFactory;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.api.algorithm.standard.PreciseShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.standard.RangeShardingAlgorithm;
import io.shardingjdbc.core.routing.strategy.standard.StandardShardingStrategy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

/**
 * Standard strategy configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public class StandardShardingStrategyConfiguration implements ShardingStrategyConfiguration {
    
    private String shardingColumn;
    
    private String preciseAlgorithmClassName;
    
    private String rangeAlgorithmClassName;
    
    @Override
    public ShardingStrategy build() {
        Preconditions.checkNotNull(shardingColumn, "Sharding column cannot be null.");
        Preconditions.checkNotNull(preciseAlgorithmClassName, "Precise algorithm class cannot be null.");
        if (Strings.isNullOrEmpty(rangeAlgorithmClassName)) {
            return new StandardShardingStrategy(shardingColumn, ShardingAlgorithmFactory.newInstance(preciseAlgorithmClassName, PreciseShardingAlgorithm.class));
        }
        return new StandardShardingStrategy(shardingColumn, ShardingAlgorithmFactory.newInstance(preciseAlgorithmClassName, PreciseShardingAlgorithm.class), 
                ShardingAlgorithmFactory.newInstance(rangeAlgorithmClassName, RangeShardingAlgorithm.class));
    }
}
