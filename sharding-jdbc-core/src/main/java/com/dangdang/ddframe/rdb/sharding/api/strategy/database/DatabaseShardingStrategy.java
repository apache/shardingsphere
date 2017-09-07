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

package com.dangdang.ddframe.rdb.sharding.api.strategy.database;

import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.PreciseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.RangeShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;

import java.util.Collection;
import java.util.Collections;

/**
 * Database sharding strategy.
 * 
 * @author zhangliang
 */
public final class DatabaseShardingStrategy extends ShardingStrategy {
    
    public DatabaseShardingStrategy() {
        super(Collections.singleton(""), new NoneDatabaseShardingAlgorithm());
    }
    
    public DatabaseShardingStrategy(final HintDatabaseShardingAlgorithm databaseShardingAlgorithm) {
        super(Collections.singleton(""), databaseShardingAlgorithm);
    }
    
    public DatabaseShardingStrategy(final String shardingColumn, final PreciseShardingAlgorithm preciseShardingAlgorithm) {
        super(shardingColumn, preciseShardingAlgorithm);
    }
    
    public DatabaseShardingStrategy(final String shardingColumn, final PreciseShardingAlgorithm preciseShardingAlgorithm, final RangeShardingAlgorithm rangeShardingAlgorithm) {
        super(shardingColumn, preciseShardingAlgorithm, rangeShardingAlgorithm);
    }
    
    public DatabaseShardingStrategy(final Collection<String> shardingColumns, final ComplexKeysDatabaseShardingAlgorithm databaseShardingAlgorithm) {
        super(shardingColumns, databaseShardingAlgorithm);
    }
}
