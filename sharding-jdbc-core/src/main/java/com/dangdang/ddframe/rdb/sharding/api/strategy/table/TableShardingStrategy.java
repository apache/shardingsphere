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

package com.dangdang.ddframe.rdb.sharding.api.strategy.table;

import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.PreciseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.RangeShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;

import java.util.Collection;
import java.util.Collections;

/**
 * Table sharding strategy.
 * 
 * @author zhangliang
 */
public final class TableShardingStrategy extends ShardingStrategy {
    
    public TableShardingStrategy() {
        super(Collections.singleton(""), new NoneTableShardingAlgorithm());
    }
    
    public TableShardingStrategy(final String shardingColumn, final PreciseShardingAlgorithm preciseShardingAlgorithm) {
        super(shardingColumn, preciseShardingAlgorithm);
    }
    
    public TableShardingStrategy(final String shardingColumn, final PreciseShardingAlgorithm preciseShardingAlgorithm, final RangeShardingAlgorithm rangeShardingAlgorithm) {
        super(shardingColumn, preciseShardingAlgorithm, rangeShardingAlgorithm);
    }
    
    public TableShardingStrategy(final Collection<String> shardingColumns, final MultipleKeysTableShardingAlgorithm tableShardingAlgorithm) {
        super(shardingColumns, tableShardingAlgorithm);
    }
}
