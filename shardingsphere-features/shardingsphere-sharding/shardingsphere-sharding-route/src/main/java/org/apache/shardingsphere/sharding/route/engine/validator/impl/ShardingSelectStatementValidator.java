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

package org.apache.shardingsphere.sharding.route.engine.validator.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.strategy.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding select statement validator.
 */
public final class ShardingSelectStatementValidator implements ShardingStatementValidator<SelectStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final RouteContext routeContext, final ShardingSphereMetaData metaData) {
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        if (1 != ((TableAvailable) sqlStatementContext).getAllTables().size()) {
            throw new ShardingSphereException("Cannot support Multiple-Table for '%s'.", sqlStatementContext.getSqlStatement());
        }
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(shardingRule, tableRule.get())
                    && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        List<Object> parameters = routeContext.getParameters();
        ShardingConditions shardingConditions = getShardingConditions(parameters, sqlStatementContext, metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData(), shardingRule);
        Preconditions.checkState(!shardingConditions.getConditions().isEmpty(), "Must have sharding column with subquery.");
        if (shardingConditions.getConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingRule, shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    @Override
    public void postValidate(final SelectStatement sqlStatement, final RouteResult routeResult) {
    
    }
    
    private ShardingConditions getShardingConditions(final List<Object> parameters,
                                                     final SQLStatementContext<?> sqlStatementContext, final SchemaMetaData schemaMetaData, final ShardingRule shardingRule) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            if (sqlStatementContext instanceof InsertStatementContext) {
                return new ShardingConditions(new InsertClauseShardingConditionEngine(shardingRule, schemaMetaData).createShardingConditions((InsertStatementContext) sqlStatementContext, parameters));
            }
            return new ShardingConditions(new WhereClauseShardingConditionEngine(shardingRule, schemaMetaData).createShardingConditions(sqlStatementContext, parameters));
        }
        return new ShardingConditions(Collections.emptyList());
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy && shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy;
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
        if (shardingCondition1.getRouteValues().size() != shardingCondition2.getRouteValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getRouteValues().size(); i++) {
            RouteValue shardingValue1 = shardingCondition1.getRouteValues().get(i);
            RouteValue shardingValue2 = shardingCondition2.getRouteValues().get(i);
            if (!isSameRouteValue(shardingRule, (ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameRouteValue(final ShardingRule shardingRule, final ListRouteValue routeValue1, final ListRouteValue routeValue2) {
        return isSameLogicTable(shardingRule, routeValue1, routeValue2) && routeValue1.getColumnName().equals(routeValue2.getColumnName())
                && SafeNumberOperationUtils.safeEquals(routeValue1.getValues(), routeValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ShardingRule shardingRule, final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingRule, shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ShardingRule shardingRule, final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
}
