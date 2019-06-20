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
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.optimize.OptimizeEngineFactory;
import org.apache.shardingsphere.core.optimize.condition.RouteCondition;
import org.apache.shardingsphere.core.optimize.condition.RouteConditions;
import org.apache.shardingsphere.core.optimize.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.parse.entry.ShardingSQLParseEntry;
import org.apache.shardingsphere.core.parse.hook.ParsingHook;
import org.apache.shardingsphere.core.parse.hook.SPIParsingHook;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.Collections;
import java.util.Iterator;
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
    
    private final ShardingMetaData shardingMetaData;
    
    private final DatabaseType databaseType;
    
    private final ParsingResultCache parsingResultCache;
    
    private final List<Comparable<?>> generatedValues = new LinkedList<>();
    
    private final ParsingHook parsingHook = new SPIParsingHook();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        parsingHook.start(logicSQL);
        try {
            SQLStatement result = new ShardingSQLParseEntry(databaseType, shardingRule, shardingMetaData.getTable(), parsingResultCache).parse(logicSQL, useCache);
            parsingHook.finishSuccess(result, shardingMetaData.getTable());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            parsingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    @Override
    public SQLRouteResult route(final SQLStatement sqlStatement, final List<Object> parameters) {
        OptimizeResult optimizeResult = OptimizeEngineFactory.newInstance(shardingRule, sqlStatement, parameters, shardingMetaData.getTable()).optimize();
        RouteConditions routeConditions = null;
        if (sqlStatement instanceof InsertStatement) {
            List<RouteCondition> routeConditionList = optimizeResult.getRouteConditions().getRouteConditions();
            appendGeneratedKeyCondition(sqlStatement, optimizeResult, routeConditionList);
            routeConditions = new RouteConditions(routeConditionList);
            optimizeResult.setRouteConditions(routeConditions);
        }
        boolean needMergeShardingValues = isNeedMergeShardingValues(sqlStatement);
        if (needMergeShardingValues) {
            checkSubqueryShardingValues(sqlStatement, optimizeResult.getRouteConditions());
            mergeShardingValues(null == routeConditions ? optimizeResult.getRouteConditions() : routeConditions);
        }
        RoutingResult routingResult = RoutingEngineFactory.newInstance(shardingRule, shardingMetaData.getDataSource(), sqlStatement, optimizeResult).route();
        if (needMergeShardingValues) {
            Preconditions.checkState(1 == routingResult.getRoutingUnits().size(), "Must have one sharding with subquery.");
        }
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        result.setRoutingResult(routingResult);
        setOptimizeResult(optimizeResult, result);
        return result;
    }
    
    private void appendGeneratedKeyCondition(final SQLStatement sqlStatement, final OptimizeResult optimizeResult, final List<RouteCondition> routeConditions) {
        if (!optimizeResult.getGeneratedKey().isPresent() || optimizeResult.getGeneratedKey().get().isGenerated()) {
            return;
        }
        String tableName = sqlStatement.getTables().getSingleTableName();
        GeneratedKey generatedKey = optimizeResult.getGeneratedKey().get();
        String generatedKeyColumnName = generatedKey.getColumnName();
        Iterator<Comparable<?>> generatedValues = generatedKey.getGeneratedValues().iterator();
        for (RouteCondition each : routeConditions) {
            if (shardingRule.isShardingColumn(generatedKeyColumnName, tableName)) {
                each.getRouteValues().add(new ListRouteValue<>(generatedKeyColumnName, tableName, Collections.<Comparable<?>>singletonList(generatedValues.next())));
            }
        }
    }
    
    private void setOptimizeResult(final OptimizeResult optimizeResult, final SQLRouteResult sqlRouteResult) {
        if (optimizeResult.getGeneratedKey().isPresent()) {
            generatedValues.addAll(optimizeResult.getGeneratedKey().get().getGeneratedValues());
            optimizeResult.getGeneratedKey().get().getGeneratedValues().clear();
            optimizeResult.getGeneratedKey().get().getGeneratedValues().addAll(generatedValues);
        }
        sqlRouteResult.setOptimizeResult(optimizeResult);
    }
    
    private boolean isNeedMergeShardingValues(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement 
                && ((SelectStatement) sqlStatement).isContainsSubquery() && !shardingRule.getShardingLogicTableNames(sqlStatement.getTables().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final SQLStatement sqlStatement, final RouteConditions routeConditions) {
        for (String each : sqlStatement.getTables().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && shardingRule.isRoutingByHint(tableRule.get()) && !HintManager.getDatabaseShardingValues(each).isEmpty()
                    && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        Preconditions.checkState(null != routeConditions.getRouteConditions() && !routeConditions.getRouteConditions().isEmpty(), "Must have sharding column with subquery.");
        if (routeConditions.getRouteConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(routeConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isSameShardingCondition(final RouteConditions routeConditions) {
        RouteCondition example = routeConditions.getRouteConditions().remove(routeConditions.getRouteConditions().size() - 1);
        for (RouteCondition each : routeConditions.getRouteConditions()) {
            if (!isSameShardingCondition(example, each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final RouteCondition routeCondition1, final RouteCondition routeCondition2) {
        if (routeCondition1.getRouteValues().size() != routeCondition2.getRouteValues().size()) {
            return false;
        }
        for (int i = 0; i < routeCondition1.getRouteValues().size(); i++) {
            RouteValue shardingValue1 = routeCondition1.getRouteValues().get(i);
            RouteValue shardingValue2 = routeCondition2.getRouteValues().get(i);
            if (!isSameShardingValue((ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingValue(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return isSameLogicTable(shardingValue1, shardingValue2)
                && shardingValue1.getColumnName().equals(shardingValue2.getColumnName()) && shardingValue1.getValues().equals(shardingValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
    
    private void mergeShardingValues(final RouteConditions routeConditions) {
        if (routeConditions.getRouteConditions().size() > 1) {
            RouteCondition routeCondition = routeConditions.getRouteConditions().remove(routeConditions.getRouteConditions().size() - 1);
            routeConditions.getRouteConditions().clear();
            routeConditions.getRouteConditions().add(routeCondition);
        }
    }
}
