/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.route.engine.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding conditions.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ShardingConditions {
    
    private final List<ShardingCondition> conditions;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final ShardingRule rule;
    
    /**
     * Judge sharding conditions is always false or not.
     *
     * @return sharding conditions is always false or not
     */
    public boolean isAlwaysFalse() {
        if (conditions.isEmpty()) {
            return false;
        }
        for (ShardingCondition each : conditions) {
            if (!(each instanceof AlwaysFalseShardingCondition)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Merge sharding conditions.
     */
    public void merge() {
        if (conditions.size() > 1) {
            List<ShardingCondition> result = new ArrayList<>();
            result.add(conditions.remove(conditions.size() - 1));
            while (conditions.size() > 0) {
                findUniqueShardingCondition(result, conditions.remove(conditions.size() - 1)).ifPresent(result::add);
            }
            conditions.addAll(result);
        }
    }
    
    private Optional<ShardingCondition> findUniqueShardingCondition(final List<ShardingCondition> conditions, final ShardingCondition condition) {
        for (ShardingCondition each : conditions) {
            if (isSameShardingCondition(rule, condition, each)) {
                return Optional.empty();
            }
        }
        return Optional.of(condition);
    }
    
    /**
     * Judge whether sharding condition need merge or not.
     *
     * @return whether sharding condition need merge or not
     */
    public boolean isNeedMerge() {
        boolean selectContainsSubquery = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery();
        boolean insertSelectContainsSubquery = sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                && ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext().isContainsSubquery();
        return (selectContainsSubquery || insertSelectContainsSubquery) && !rule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames()).isEmpty();
    }
    
    /**
     * Judge whether all sharding conditions are same or not.
     *
     * @return whether all sharding conditions are same or not
     */
    public boolean isSameShardingCondition() {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = rule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(rule, tableRule.get())
                    && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return false;
            }
        }
        return conditions.size() <= 1;
    }
    
    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getValues().size() != shardingCondition2.getValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getValues().size(); i++) {
            ShardingConditionValue shardingConditionValue1 = shardingCondition1.getValues().get(i);
            ShardingConditionValue shardingConditionValue2 = shardingCondition2.getValues().get(i);
            if (!isSameShardingConditionValue(shardingRule, shardingConditionValue1, shardingConditionValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration
                && shardingRule.getTableShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration;
    }
    
    private boolean isSameShardingConditionValue(final ShardingRule shardingRule, final ShardingConditionValue shardingConditionValue1, final ShardingConditionValue shardingConditionValue2) {
        return isSameLogicTable(shardingRule, shardingConditionValue1, shardingConditionValue2) && shardingConditionValue1.getColumnName().equals(shardingConditionValue2.getColumnName())
                && isSameValue(shardingConditionValue1, shardingConditionValue2);
    }
    
    private boolean isSameLogicTable(final ShardingRule shardingRule, final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingRule, shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ShardingRule shardingRule, final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean isSameValue(final ShardingConditionValue shardingConditionValue1, final ShardingConditionValue shardingConditionValue2) {
        if (shardingConditionValue1 instanceof ListShardingConditionValue && shardingConditionValue2 instanceof ListShardingConditionValue) {
            return SafeNumberOperationUtil.safeCollectionEquals(
                    ((ListShardingConditionValue) shardingConditionValue1).getValues(), ((ListShardingConditionValue) shardingConditionValue2).getValues());
        } else if (shardingConditionValue1 instanceof RangeShardingConditionValue && shardingConditionValue2 instanceof RangeShardingConditionValue) {
            return SafeNumberOperationUtil.safeRangeEquals(
                    ((RangeShardingConditionValue) shardingConditionValue1).getValueRange(), ((RangeShardingConditionValue) shardingConditionValue2).getValueRange());
        }
        return false;
    }
}
