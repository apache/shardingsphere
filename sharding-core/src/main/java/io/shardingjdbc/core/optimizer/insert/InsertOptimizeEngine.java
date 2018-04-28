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

package io.shardingjdbc.core.optimizer.insert;

import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.optimizer.OptimizeEngine;
import io.shardingjdbc.core.optimizer.condition.ShardingCondition;
import io.shardingjdbc.core.optimizer.condition.ShardingConditions;
import io.shardingjdbc.core.parsing.parser.context.condition.AndCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.OrCondition;
import io.shardingjdbc.core.routing.router.sharding.GeneratedKey;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class InsertOptimizeEngine implements OptimizeEngine {
    
    private final OrCondition orCondition;
    
    private final List<Object> parameters;
    
    private final GeneratedKey generatedKey;
    
    @Override
    public ShardingConditions optimize() {
        ShardingCondition result = new ShardingCondition();
        for (AndCondition each : orCondition.getAndConditions()) {
            result.getShardingValues().addAll(getShardingCondition(each));
        }
        if (null != generatedKey) {
            result.getShardingValues().add(getShardingCondition(generatedKey));
        }
        return new ShardingConditions(result.getShardingValues().isEmpty() ? Collections.<ShardingCondition>emptyList() : Collections.singletonList(result));
    }
    
    private Collection<ListShardingValue> getShardingCondition(final AndCondition andCondition) {
        Collection<ListShardingValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListShardingValue<>(each.getColumn().getTableName(), each.getColumn().getName(), each.getConditionValues(parameters)));
        }
        return result;
    }
    
    private ListShardingValue getShardingCondition(final GeneratedKey generatedKey) {
        return new ListShardingValue<>(generatedKey.getColumn().getTableName(), generatedKey.getColumn().getName(), 
                new GeneratedKeyCondition(generatedKey.getColumn(), generatedKey.getIndex(), generatedKey.getValue()).getConditionValues(parameters));
    }
}
