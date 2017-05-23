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

package com.dangdang.ddframe.rdb.sharding.routing.fixture;

import java.util.Collection;
import java.util.HashSet;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;

public final class OrderAttrShardingAlgorithm implements SingleKeyTableShardingAlgorithm<Integer> {
    
    @Override
    public String doEqualSharding(final Collection<String> tables, final ShardingValue<Integer> shardingValue) {
        String suffix = shardingValue.getValue() % 2 == 0 ? "_a" : "_b";
        for (String each : tables) {
            if (each.endsWith(suffix)) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public Collection<String> doInSharding(final Collection<String> tables, final ShardingValue<Integer> shardingValue) {
        Collection<String> result = new HashSet<>(tables.size());
        for (int value : shardingValue.getValues()) {
            String suffix = value % 2 == 0 ? "_a" : "_b";
            for (String table : tables) {
                if (table.endsWith(suffix)) {
                    result.add(table);
                }
            }
        }
        return result;
    }
    
    @Override
    public Collection<String> doBetweenSharding(final Collection<String> availableTargetNames, final ShardingValue<Integer> shardingValue) {
        throw new UnsupportedOperationException();
    }
}
