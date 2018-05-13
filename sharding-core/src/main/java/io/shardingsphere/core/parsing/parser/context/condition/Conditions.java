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

package io.shardingsphere.core.parsing.parser.context.condition;

import com.google.common.base.Optional;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Conditions collection.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
@NoArgsConstructor
@Getter
@ToString
public final class Conditions {
    
    private final OrCondition orCondition = new OrCondition();
    
    public Conditions(final Conditions conditions) {
        orCondition.getAndConditions().addAll(conditions.orCondition.getAndConditions());
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
            orCondition.add(condition);
        }
    }
    
    /**
     * Find condition via column in first and condition.
     *
     * @param column column
     * @return found condition
     * @deprecated only test call
     */
    @Deprecated
    public Optional<Condition> find(final Column column) {
        return find(column, 0);
    }
    
    /**
     * Find condition via column.
     *
     * @param column column
     * @param index index of and conditions
     * @return found condition
     * @deprecated only test call
     */
    @Deprecated
    public Optional<Condition> find(final Column column, final int index) {
        return orCondition.find(column, index);
    }
}
