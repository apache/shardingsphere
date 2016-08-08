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

package com.dangdang.ddframe.rdb.sharding.api.strategy.fixture;

import java.util.ArrayList;
import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.router.strategy.SingleKeyShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingAlgorithm;

public final class TestSingleKeyShardingAlgorithm implements SingleKeyShardingAlgorithm<String>, DatabaseShardingAlgorithm, TableShardingAlgorithm {
    
    @Override
    public String doEqualSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        return shardingValue.getValue();
    }
    
    @Override
    public Collection<String> doInSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        return shardingValue.getValues();
    }
    
    @Override
    public Collection<String> doBetweenSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        Collection<String> result = new ArrayList<>();
        for (Integer i = Integer.parseInt(shardingValue.getValueRange().lowerEndpoint()); i <= Integer.parseInt(shardingValue.getValueRange().upperEndpoint()); i++) {
            result.add(i.toString());
        }
        return result;
    }
}
