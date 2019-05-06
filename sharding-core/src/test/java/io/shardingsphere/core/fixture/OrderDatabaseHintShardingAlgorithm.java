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

package io.shardingsphere.core.fixture;

import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.api.algorithm.sharding.hint.HintShardingAlgorithm;

import java.util.Collection;
import java.util.Collections;

public final class OrderDatabaseHintShardingAlgorithm implements HintShardingAlgorithm {
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final ShardingValue shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(String.valueOf((int) ((ListShardingValue) shardingValue).getValues().iterator().next() % 2))) {
                return Collections.singletonList(each);
            }
        }
        return null;
    }
}
