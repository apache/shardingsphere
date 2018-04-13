/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.routing.sharding;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Sharding conditions.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class ShardingConditions {
    
    private final GeneratedKey generatedKey;
    
    private final List<ShardingCondition> shardingConditions = new LinkedList<>();
    
    /**
     * Add sharding value.
     *
     * @param shardingCondition sharding condition
     */
    public void add(final ShardingCondition shardingCondition) {
        shardingConditions.add(shardingCondition);
    }
    
    /**
     * Get sharding condition via index.
     *
     * @param index index of sharding conditions
     * @return found and conditions
     */
    public Optional<ShardingCondition> get(final int index) {
        ShardingCondition result = null;
        if (size() > index) {
            result = shardingConditions.get(index);
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Adjust sharding conditions is empty or not.
     *
     * @return sharding conditions is empty or not
     */
    public boolean isEmpty() {
        return shardingConditions.isEmpty();
    }
    
    /**
     * Returns the number of sharding conditions in this.
     *
     * @return the number of sharding conditions in this
     */
    public int size() {
        return shardingConditions.size();
    }
}
