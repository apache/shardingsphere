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
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SafeNumberOperationUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding conditions.
 */
@Getter
@ToString
public final class ShardingConditions {
    
    private final List<ShardingCondition> conditions;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShardingRule rule;
    
    private final boolean subqueryContainsShardingCondition;
    
    public ShardingConditions(final List<ShardingCondition> conditions, final SQLStatementContext sqlStatementContext, final ShardingRule rule) {
        this.conditions = conditions;
        this.sqlStatementContext = sqlStatementContext;
        this.rule = rule;
        subqueryContainsShardingCondition = isSubqueryContainsShardingCondition(conditions, sqlStatementContext);
    }
    
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
            Collection<ShardingCondition> result = new LinkedList<>();
            result.add(conditions.remove(conditions.size() - 1));
            while (!conditions.isEmpty()) {
                findUniqueShardingCondition(result, conditions.remove(conditions.size() - 1)).ifPresent(result::add);
            }
            conditions.addAll(result);
        }
    }
    
    private Optional<ShardingCondition> findUniqueShardingCondition(final Collection<ShardingCondition> conditions, final ShardingCondition condition) {
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
    
    private boolean isSubqueryContainsShardingCondition(final List<ShardingCondition> conditions, final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatement> selectStatements = getSelectStatements(sqlStatementContext);
        if (selectStatements.size() > 1) {
            Map<Integer, List<ShardingCondition>> startIndexShardingConditions = new HashMap<>(conditions.size(), 1F);
            for (ShardingCondition each : conditions) {
                startIndexShardingConditions.computeIfAbsent(each.getStartIndex(), unused -> new LinkedList<>()).add(each);
            }
            for (SelectStatement each : selectStatements) {
                if (each.getFrom().isPresent() && each.getFrom().get() instanceof SubqueryTableSegment) {
                    continue;
                }
                if (!each.getWhere().isPresent() || !startIndexShardingConditions.containsKey(each.getWhere().get().getExpr().getStartIndex())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private Collection<SelectStatement> getSelectStatements(final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatement> result = new LinkedList<>();
        if (sqlStatementContext instanceof SelectStatementContext) {
            result.add(((SelectStatementContext) sqlStatementContext).getSqlStatement());
            for (SelectStatementContext each : ((SelectStatementContext) sqlStatementContext).getSubqueryContexts().values()) {
                result.add(each.getSqlStatement());
            }
        }
        if (sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()) {
            SelectStatementContext selectStatementContext = ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext();
            result.add(selectStatementContext.getSqlStatement());
            for (SelectStatementContext each : selectStatementContext.getSubqueryContexts().values()) {
                result.add(each.getSqlStatement());
            }
        }
        return result;
    }
    
    /**
     * Judge whether all sharding conditions are same or not.
     *
     * @return whether all sharding conditions are same or not
     */
    public boolean isSameShardingCondition() {
        Collection<String> hintStrategyTables = findHintStrategyTables(sqlStatementContext);
        return 1 == hintStrategyTables.size() || subqueryContainsShardingCondition && 1 == conditions.size();
    }
    
    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getValues().size() != shardingCondition2.getValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getValues().size(); i++) {
            ShardingConditionValue shardingValue1 = shardingCondition1.getValues().get(i);
            ShardingConditionValue shardingValue2 = shardingCondition2.getValues().get(i);
            if (!isSameShardingConditionValue(shardingRule, shardingValue1, shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) && shardingValue1.getColumnName().equals(shardingValue2.getColumnName());
    }
    
    private Collection<String> findHintStrategyTables(final SQLStatementContext sqlStatementContext) {
        Collection<String> result = new HashSet<>(sqlStatementContext.getTablesContext().getTableNames().size(), 1F);
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<ShardingTable> shardingTable = rule.findShardingTable(each);
            if (!shardingTable.isPresent()) {
                continue;
            }
            ShardingStrategyConfiguration databaseHintStrategy = rule.getDatabaseShardingStrategyConfiguration(shardingTable.get());
            ShardingStrategyConfiguration tableHintStrategy = rule.getTableShardingStrategyConfiguration(shardingTable.get());
            boolean isDatabaseTableHintStrategy = databaseHintStrategy instanceof HintShardingStrategyConfiguration && tableHintStrategy instanceof HintShardingStrategyConfiguration;
            boolean isDatabaseHintStrategy = databaseHintStrategy instanceof HintShardingStrategyConfiguration && tableHintStrategy instanceof NoneShardingStrategyConfiguration;
            boolean isTableHintStrategy = databaseHintStrategy instanceof NoneShardingStrategyConfiguration && tableHintStrategy instanceof HintShardingStrategyConfiguration;
            if (isDatabaseTableHintStrategy || isDatabaseHintStrategy || isTableHintStrategy) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isBindingTable(final ShardingRule shardingRule, final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && !shardingValue1.getTableName().equals(shardingValue2.getTableName()) && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
    
    private boolean isSameShardingConditionValue(final ShardingRule shardingRule, final ShardingConditionValue shardingValue1, final ShardingConditionValue shardingValue2) {
        if (isBindingTable(shardingRule, shardingValue1, shardingValue2)) {
            return true;
        }
        return isSameShardingCondition(shardingValue1, shardingValue2) && isSameShardingValue(shardingValue1, shardingValue2);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean isSameShardingValue(final ShardingConditionValue shardingConditionValue1, final ShardingConditionValue shardingConditionValue2) {
        if (shardingConditionValue1 instanceof ListShardingConditionValue && shardingConditionValue2 instanceof ListShardingConditionValue) {
            return SafeNumberOperationUtils.safeCollectionEquals(
                    ((ListShardingConditionValue) shardingConditionValue1).getValues(), ((ListShardingConditionValue) shardingConditionValue2).getValues());
        }
        if (shardingConditionValue1 instanceof RangeShardingConditionValue && shardingConditionValue2 instanceof RangeShardingConditionValue) {
            return SafeNumberOperationUtils.safeRangeEquals(
                    ((RangeShardingConditionValue) shardingConditionValue1).getValueRange(), ((RangeShardingConditionValue) shardingConditionValue2).getValueRange());
        }
        return false;
    }
}
