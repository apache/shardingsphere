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

package org.apache.shardingsphere.sharding.route.engine.validator.dml.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding delete statement validator.
 */
public final class ShardingSelectStatementValidator extends ShardingDMLStatementValidator<SelectStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<SelectStatement> sqlStatementContext, 
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(shardingRule, tableRule.get())
                && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        ShardingConditions shardingConditions = createShardingConditions(sqlStatementContext, parameters, schema, shardingRule);
        if (shardingConditions.getConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingRule, shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration
            && shardingRule.getTableShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration;
    }

    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getConditions()) {
            if (!isSameShardingCondition(shardingRule, example, each)) {
                return false;
            }
        }
        return true;
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
            return SafeNumberOperationUtils.safeCollectionEquals(
                ((ListShardingConditionValue) shardingConditionValue1).getValues(), ((ListShardingConditionValue) shardingConditionValue2).getValues());
        } else if (shardingConditionValue1 instanceof RangeShardingConditionValue && shardingConditionValue2 instanceof RangeShardingConditionValue) {
            return SafeNumberOperationUtils.safeRangeEquals(
                ((RangeShardingConditionValue) shardingConditionValue1).getValueRange(), ((RangeShardingConditionValue) shardingConditionValue2).getValueRange());
        }
        return false;
    }

    private ShardingConditions createShardingConditions(final SQLStatementContext<SelectStatement> sqlStatementContext,
        final List<Object> parameters, final ShardingSphereSchema schema, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions;
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingConditionEngine shardingConditionEngine = new WhereClauseShardingConditionEngine(rule, schema);
            shardingConditions = shardingConditionEngine.createShardingConditions(sqlStatementContext, parameters);
        } else {
            shardingConditions = Collections.emptyList();
        }
        return new ShardingConditions(shardingConditions);
    }
    
    @Override
    public void postValidate(final SelectStatement sqlStatement, final RouteContext routeContext) {
        
    }
    
}
