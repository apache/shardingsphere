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

package io.shardingjdbc.core.routing.condition;

import com.google.common.base.Optional;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Sharding conditions.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class ShardingConditions {
    
    private final List<Object> parameters;
    
    private final Conditions conditions;
    
    private final GeneratedKey generatedKey;
    
    /**
     * Get sharding values.
     * 
     * @param logicTableName logic table name
     * @param shardingColumns sharding columns
     * @return sharding values
     */
    public List<ShardingValue> getShardingValues(final String logicTableName, final Collection<String> shardingColumns) {
        List<ShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<Condition> condition = conditions.find(new Column(each, logicTableName));
            if (condition.isPresent()) {
                result.add(condition.get().getShardingValue(parameters));
            } else if (null != generatedKey && each.equals(generatedKey.getColumn())) {
                Comparable key = null == generatedKey.getValue() ? (Comparable) parameters.get(generatedKey.getIndex()) : (Comparable) generatedKey.getValue();
                result.add(new ListShardingValue<>(logicTableName, each, Collections.singletonList(key)));
            }
        }
        return result;
    }
}
