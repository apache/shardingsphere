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

import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;

/**
 * 无需分表的分片算法.
 * 
 * @author zhangliang
 */
public final class NoneTableShardingAlgorithm implements SingleKeyTableShardingAlgorithm<String>, MultipleKeysTableShardingAlgorithm {
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTableNames, final Collection<ShardingValue<?>> shardingValues) {
        return availableTableNames;
    }
    
    @Override
    public String doEqualSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        return availableTargetNames.isEmpty() ? null : availableTargetNames.iterator().next();
    }
    
    @Override
    public Collection<String> doInSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        return availableTargetNames;
    }
    
    @Override
    public Collection<String> doBetweenSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        return availableTargetNames;
    }
}
