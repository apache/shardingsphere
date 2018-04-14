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

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Sharding conditions.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
@NoArgsConstructor
public class ShardingConditions {
    
    private final List<ShardingCondition> shardingConditions = new ArrayList<>();
    
    /**
     * Adjust sharding conditions is always false.
     *
     * @return sharding conditions is always false
     */
    public boolean isAlwaysFalse() {
        if (isEmpty()) {
            return false;
        }
        for (ShardingCondition shardingCondition : shardingConditions) {
            if (!(shardingCondition instanceof AlwaysFalseShardingCondition)) {
                return false;
            }
        }
        return true;
    }
    
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
     * @return index of sharding conditions
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public ShardingCondition get(final int index) {
        return shardingConditions.get(index);
    }
    
    /**
     * Adjust sharding conditions is empty or not.
     *
     * @return sharding conditions is empty or not
     */
    public boolean isEmpty() {
        return shardingConditions.isEmpty();
    }
}
