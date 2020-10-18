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

package org.apache.shardingsphere.sharding.route.engine;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidatorFactory;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding SQL router.
 */
public final class ShardingSQLRouter implements SQLRouter<ShardingRule> {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereSchema schema, final ShardingRule rule, final ConfigurationProperties props) {
        RouteContext result = new RouteContext();
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        Optional<ShardingStatementValidator> validator = ShardingStatementValidatorFactory.newInstance(sqlStatement);
        validator.ifPresent(v -> v.preValidate(rule, logicSQL.getSqlStatementContext(), logicSQL.getParameters(), schema.getMetaData()));
        ShardingConditions shardingConditions = createShardingConditions(logicSQL, schema, rule);
        boolean needMergeShardingValues = isNeedMergeShardingValues(logicSQL.getSqlStatementContext(), rule);
        if (sqlStatement instanceof DMLStatement && needMergeShardingValues) {
            checkSubqueryShardingValues(logicSQL.getSqlStatementContext(), rule, shardingConditions);
            mergeShardingConditions(shardingConditions);
        }
        ShardingRouteEngineFactory.newInstance(rule, schema.getMetaData(), logicSQL.getSqlStatementContext(), shardingConditions, props).route(result, rule);
        validator.ifPresent(v -> v.postValidate(sqlStatement, result));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ShardingConditions createShardingConditions(final LogicSQL logicSQL, final ShardingSphereSchema schema, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions;
        if (logicSQL.getSqlStatementContext().getSqlStatement() instanceof DMLStatement) {
            ShardingConditionEngine shardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(logicSQL, schema, rule);
            shardingConditions = shardingConditionEngine.createShardingConditions(logicSQL.getSqlStatementContext(), logicSQL.getParameters());
        } else {
            shardingConditions = Collections.emptyList();
        }
        return new ShardingConditions(shardingConditions);
    }
    
    private boolean isNeedMergeShardingValues(final SQLStatementContext<?> sqlStatementContext, final ShardingRule rule) {
        boolean selectContainsSubquery = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery();
        boolean insertSelectContainsSubquery = sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                && ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext().isContainsSubquery();
        return (selectContainsSubquery || insertSelectContainsSubquery) && !rule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final SQLStatementContext<?> sqlStatementContext, final ShardingRule shardingRule, final ShardingConditions shardingConditions) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(shardingRule, tableRule.get())
                    && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        Preconditions.checkState(!shardingConditions.getConditions().isEmpty(), "Must have sharding column with subquery.");
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
            if (!isSameShardingConditionValue(shardingRule, (ListShardingConditionValue) shardingConditionValue1, (ListShardingConditionValue) shardingConditionValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingConditionValue(final ShardingRule shardingRule, final ListShardingConditionValue shardingConditionValue1, final ListShardingConditionValue shardingConditionValue2) {
        return isSameLogicTable(shardingRule, shardingConditionValue1, shardingConditionValue2) && shardingConditionValue1.getColumnName().equals(shardingConditionValue2.getColumnName()) 
                && SafeNumberOperationUtils.safeEquals(shardingConditionValue1.getValues(), shardingConditionValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ShardingRule shardingRule, final ListShardingConditionValue shardingValue1, final ListShardingConditionValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingRule, shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ShardingRule shardingRule, final ListShardingConditionValue shardingValue1, final ListShardingConditionValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
    
    private void mergeShardingConditions(final ShardingConditions shardingConditions) {
        if (shardingConditions.getConditions().size() > 1) {
            ShardingCondition shardingCondition = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
            shardingConditions.getConditions().clear();
            shardingConditions.getConditions().add(shardingCondition);
        }
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, 
                                     final LogicSQL logicSQL, final ShardingSphereSchema schema, final ShardingRule rule, final ConfigurationProperties props) {
        // TODO
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
