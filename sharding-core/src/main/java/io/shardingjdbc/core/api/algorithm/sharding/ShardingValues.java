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

package io.shardingjdbc.core.api.algorithm.sharding;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * Sharding values collection.
 * 
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@ToString
public class ShardingValues {
    
    private final List<ShardingValueUnit> shardingValueUnits = new LinkedList<>();
    
    /**
     * Add sharding value unit.
     *
     * @param shardingValueUnit sharding value unit
     */
    public void add(final ShardingValueUnit shardingValueUnit) {
        shardingValueUnits.add(shardingValueUnit);
    }
    
    /**
     * Get and condition via index.
     *
     * @param index index of and conditions
     * @return found and conditions
     */
    public Optional<ShardingValueUnit> get(final int index) {
        ShardingValueUnit result = null;
        if (size() > index) {
            result = shardingValueUnits.get(index);
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Adjust sharding value units is empty or not.
     *
     * @return and conditions is empty or not
     */
    public boolean isEmpty() {
        return shardingValueUnits.isEmpty();
    }
    
    /**
     * Returns the number of sharding value units in this.
     *
     * @return the number of sharding value units in this
     */
    public int size() {
        return shardingValueUnits.size();
    }
}
