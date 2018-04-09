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
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Conditions collection.
 *
 * @author zhangliang
 */
@NoArgsConstructor
@Getter
@ToString
public final class Conditions {
    
    private final OrConditions orConditions = new OrConditions();
    
    public Conditions(final Conditions conditions) {
        orConditions.getAndConditions().addAll(conditions.orConditions.getAndConditions());
    }
    
    /**
     * Add condition.
     *
     * @param condition condition
     * @param shardingRule databases and tables sharding rule
     */
    public void add(final Condition condition, final ShardingRule shardingRule) {
        // TODO self-join has problem, table name maybe use alias
        if (shardingRule.isShardingColumn(condition.getColumn())) {
            orConditions.add(condition);
        }
    }
    
    /**
     * Find condition via column in first and conditions.
     *
     * @param column column
     * @return found condition
     */
    public Optional<Condition> find(final Column column) {
        return find(column, 0);
    }
    
    /**
     * Find condition via column in index and conditions.
     *
     * @param column column
     * @param index index of and conditions
     * @return found condition
     */
    public Optional<Condition> find(final Column column, final int index) {
        return orConditions.find(column, index);
    }
}
