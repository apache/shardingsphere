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

package io.shardingjdbc.core.parsing.parser.context.condition;

import com.google.common.base.Optional;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Conditions collection.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class Conditions {
    
    private final Map<Column, Condition> conditions = new LinkedHashMap<>();
    
    public Conditions(final Conditions conditions) {
        for (Entry<Column, Condition> entry : conditions.conditions.entrySet()) {
            this.conditions.put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Add condition.
     *
     * @param condition condition
     * @param shardingRule databases and tables sharding rule
     */
    // TODO adjust before add condition, eg: if condition exist = operator and include same column, should remove condition (tow equal condition should found nothing)
    public void add(final Condition condition, final ShardingRule shardingRule) {
        // TODO self-join has problem, table name maybe use alias
        if (shardingRule.isShardingColumn(condition.getColumn())) {
            conditions.put(condition.getColumn(), condition);
        }
    }
    
    /**
     * Adjust condition is empty or not.
     * 
     * @return condition is empty or not
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
    
    /**
     * Find condition via column.
     *
     * @param column column
     * @return found condition
     */
    public Optional<Condition> find(final Column column) {
        return Optional.fromNullable(conditions.get(column));
    }
}
