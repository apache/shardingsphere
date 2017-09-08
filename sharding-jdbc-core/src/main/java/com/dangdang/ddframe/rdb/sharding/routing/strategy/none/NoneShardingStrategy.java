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

package com.dangdang.ddframe.rdb.sharding.routing.strategy.none;

import com.dangdang.ddframe.rdb.sharding.api.strategy.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * None sharding strategy.
 * 
 * @author zhangliang
 */
@Getter
public final class NoneShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns = Collections.emptyList();
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue> shardingValues) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(availableTargetNames);
        return result;
    }
}
