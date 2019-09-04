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

package org.apache.shardingsphere.core.route.router.sharding;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.EncryptOptimizeEngineFactory;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.ShardingOptimizeEngineFactory;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.LinkedList;
import java.util.List;

/**
 * Sharding router with parse.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class ParsingSQLRouter implements ShardingRouter {
    
    private final ShardingRule shardingRule;
    
    private final ShardingSphereMetaData metaData;
    
    private final SQLParseEngine parseEngine;
    
    private final List<Comparable<?>> generatedValues = new LinkedList<>();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        return parseEngine.parse(logicSQL, useCache);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        ShardingOptimizedStatement shardingStatement = ShardingOptimizeEngineFactory.newInstance(sqlStatement).optimize(shardingRule, metaData.getTables(), logicSQL, parameters, sqlStatement);
        boolean needMergeShardingValues = isNeedMergeShardingValues(shardingStatement);
        if (shardingStatement instanceof ShardingConditionOptimizedStatement && needMergeShardingValues) {
            checkSubqueryShardingValues(shardingStatement, ((ShardingConditionOptimizedStatement) shardingStatement).getShardingConditions());
            mergeShardingConditions(((ShardingConditionOptimizedStatement) shardingStatement).getShardingConditions());
        }
        RoutingResult routingResult = RoutingEngineFactory.newInstance(shardingRule, metaData.getDataSources(), shardingStatement).route();
        if (needMergeShardingValues) {
            Preconditions.checkState(1 == routingResult.getRoutingUnits().size(), "Must have one sharding with subquery.");
        }
        if (shardingStatement instanceof ShardingInsertOptimizedStatement) {
            setGeneratedValues((ShardingInsertOptimizedStatement) shardingStatement);
        }
        EncryptOptimizedStatement encryptStatement = EncryptOptimizeEngineFactory.newInstance(sqlStatement)
                .optimize(shardingRule.getEncryptRule(), metaData.getTables(), logicSQL, parameters, sqlStatement);
        SQLRouteResult result = new SQLRouteResult(shardingStatement, encryptStatement);
        result.setRoutingResult(routingResult);
        return result;
    }
    
    private void setGeneratedValues(final ShardingInsertOptimizedStatement optimizedStatement) {
        if (optimizedStatement.getGeneratedKey().isPresent()) {
            generatedValues.addAll(optimizedStatement.getGeneratedKey().get().getGeneratedValues());
            optimizedStatement.getGeneratedKey().get().getGeneratedValues().clear();
            optimizedStatement.getGeneratedKey().get().getGeneratedValues().addAll(generatedValues);
        }
    }
    
    private boolean isNeedMergeShardingValues(final OptimizedStatement optimizedStatement) {
        return optimizedStatement instanceof ShardingSelectOptimizedStatement && ((ShardingSelectOptimizedStatement) optimizedStatement).isContainsSubquery() 
                && !shardingRule.getShardingLogicTableNames(optimizedStatement.getTables().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final OptimizedStatement optimizedStatement, final ShardingConditions shardingConditions) {
        for (String each : optimizedStatement.getTables().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && shardingRule.isRoutingByHint(tableRule.get()) && !HintManager.getDatabaseShardingValues(each).isEmpty()
                    && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        Preconditions.checkState(null != shardingConditions.getConditions() && !shardingConditions.getConditions().isEmpty(), "Must have sharding column with subquery.");
        if (shardingConditions.getConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isSameShardingCondition(final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getConditions()) {
            if (!isSameShardingCondition(example, each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getRouteValues().size() != shardingCondition2.getRouteValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getRouteValues().size(); i++) {
            RouteValue shardingValue1 = shardingCondition1.getRouteValues().get(i);
            RouteValue shardingValue2 = shardingCondition2.getRouteValues().get(i);
            if (!isSameRouteValue((ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameRouteValue(final ListRouteValue routeValue1, final ListRouteValue routeValue2) {
        return isSameLogicTable(routeValue1, routeValue2)
                && routeValue1.getColumnName().equals(routeValue2.getColumnName()) && routeValue1.getValues().equals(routeValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
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
}
