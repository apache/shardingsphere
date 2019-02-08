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

package org.apache.shardingsphere.core.routing.router.sharding;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.hint.HintManagerHolder;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.optimizer.OptimizeEngineFactory;
import org.apache.shardingsphere.core.optimizer.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.SQLParsingEngine;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Conditions;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.routing.RouteUnit;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.routing.type.RoutingResult;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.routing.value.RouteValue;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.spi.hook.SPIParsingHook;
import org.apache.shardingsphere.core.util.SQLLogger;
import org.apache.shardingsphere.spi.hook.ParsingHook;

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
    
    private final boolean showSQL;
    
    private final List<Comparable<?>> generatedKeys = new LinkedList<>();
    
    private final ParsingHook parsingHook = new SPIParsingHook();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        parsingHook.start(logicSQL);
        try {
            SQLStatement result = new SQLParsingEngine(databaseType, logicSQL, shardingRule, shardingMetaData.getTable()).parse(useCache);
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
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        Optional<GeneratedKey> generatedKey = sqlStatement instanceof InsertStatement ? getGenerateKey(parameters, (InsertStatement) sqlStatement) : Optional.<GeneratedKey>absent();
        SQLRouteResult result = new SQLRouteResult(sqlStatement, generatedKey.orNull());
        ShardingConditions shardingConditions = OptimizeEngineFactory.newInstance(shardingRule, sqlStatement, parameters, generatedKey.orNull()).optimize();
        if (generatedKey.isPresent()) {
            setGeneratedKeys(result, generatedKey.get());
        }
        if (sqlStatement instanceof SelectStatement && isNeedMergeShardingValues((SelectStatement) sqlStatement)) {
            checkSubqueryShardingValues(sqlStatement, sqlStatement.getConditions(), shardingConditions);
            mergeShardingValues(shardingConditions);
        }
        RoutingResult routingResult = RoutingEngineFactory.newInstance(shardingRule, shardingMetaData.getDataSource(), sqlStatement, shardingConditions).route();
        boolean isSingleRouting = routingResult.isSingleRouting();
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, logicSQL, databaseType, sqlStatement, shardingConditions, parameters);
        if (sqlStatement instanceof SelectStatement && null != ((SelectStatement) sqlStatement).getLimit()) {
            processLimit(parameters, (SelectStatement) sqlStatement, isSingleRouting);
        }
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(isSingleRouting);
        for (TableUnit each : routingResult.getTableUnits().getTableUnits()) {
            result.getRouteUnits().add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, shardingMetaData.getDataSource())));
        }
        if (showSQL) {
            SQLLogger.logSQL(logicSQL, sqlStatement, result.getRouteUnits());
        }
        return result;
    }
    
    private Optional<GeneratedKey> getGenerateKey(final List<Object> parameters, final InsertStatement insertStatement) {
        return -1 == insertStatement.getGenerateKeyColumnIndex() ? createGeneratedKey(insertStatement) : findGeneratedKey(parameters, insertStatement);
    }
    
    private Optional<GeneratedKey> createGeneratedKey(final InsertStatement insertStatement) {
        String logicTableName = insertStatement.getTables().getSingleTableName();
        Optional<TableRule> tableRule = shardingRule.findTableRule(logicTableName);
        if (!tableRule.isPresent()) {
            return Optional.absent();
        }
        Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(logicTableName);
        if (!generateKeyColumn.isPresent()) {
            return Optional.absent();
        }
        GeneratedKey result = new GeneratedKey(generateKeyColumn.get());
        for (int i = 0; i < insertStatement.getInsertValues().getInsertValues().size(); i++) {
            result.getGeneratedKeys().add(shardingRule.generateKey(logicTableName));
        }
        return Optional.of(result);
    }
    
    private Optional<GeneratedKey> findGeneratedKey(final List<Object> parameters, final InsertStatement insertStatement) {
        GeneratedKey result = null;
        for (GeneratedKeyCondition each : insertStatement.getGeneratedKeyConditions()) {
            if (null == result) {
                result = new GeneratedKey(each.getColumn());
            }
            result.getGeneratedKeys().add(-1 == each.getIndex() ? each.getValue() : (Comparable<?>) parameters.get(each.getIndex()));
        }
        return Optional.fromNullable(result);
    }
    
    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final GeneratedKey generatedKey) {
        generatedKeys.addAll(generatedKey.getGeneratedKeys());
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().addAll(generatedKeys);
    }
    
    private boolean isNeedMergeShardingValues(final SelectStatement selectStatement) {
        return !selectStatement.getSubqueryConditions().isEmpty() && !shardingRule.getShardingLogicTableNames(selectStatement.getTables().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final SQLStatement sqlStatement, final Conditions conditions, final ShardingConditions shardingConditions) {
        for (String each : sqlStatement.getTables().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && shardingRule.isRoutingByHint(tableRule.get()) && HintManagerHolder.getDatabaseShardingValue(each).isPresent()
                    && HintManagerHolder.getTableShardingValue(each).isPresent()) {
                return;
            }
        }
        Preconditions.checkState(!shardingConditions.getShardingConditions().isEmpty(), "Must have sharding column with subquery.");
        Preconditions.checkState(isShardingOperatorAllEqual(conditions), "Only support sharding by '=' with subquery.");
        Preconditions.checkState(isListShardingValue(shardingConditions), "Only support sharding by '=' with subquery.");
        if (shardingConditions.getShardingConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isShardingOperatorAllEqual(final Conditions conditions) {
        for (AndCondition each : conditions.getOrCondition().getAndConditions()) {
            if (!isShardingOperatorAllEqual(each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isShardingOperatorAllEqual(final AndCondition andCondition) {
        for (Condition each : andCondition.getConditions()) {
            if (ShardingOperator.EQUAL != each.getOperator()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isListShardingValue(final ShardingConditions shardingConditions) {
        for (ShardingCondition each : shardingConditions.getShardingConditions()) {
            if (!isListShardingValue(each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isListShardingValue(final ShardingCondition shardingCondition) {
        for (RouteValue each : shardingCondition.getShardingValues()) {
            if (!(each instanceof ListRouteValue)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getShardingConditions().remove(shardingConditions.getShardingConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getShardingConditions()) {
            if (!isSameShardingCondition(example, each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getShardingValues().size() != shardingCondition2.getShardingValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getShardingValues().size(); i++) {
            RouteValue shardingValue1 = shardingCondition1.getShardingValues().get(i);
            RouteValue shardingValue2 = shardingCondition2.getShardingValues().get(i);
            if (!isSameShardingValue((ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingValue(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return isSameLogicTable(shardingValue1, shardingValue2)
                && shardingValue1.getColumn().getName().equals(shardingValue2.getColumn().getName()) && shardingValue1.getValues().equals(shardingValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getColumn().getTableName().equals(shardingValue2.getColumn().getTableName()) || isBindingTable(shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getColumn().getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getColumn().getTableName());
    }
    
    private void mergeShardingValues(final ShardingConditions shardingConditions) {
        if (shardingConditions.getShardingConditions().size() > 1) {
            ShardingCondition shardingCondition = shardingConditions.getShardingConditions().remove(shardingConditions.getShardingConditions().size() - 1);
            shardingConditions.getShardingConditions().clear();
            shardingConditions.getShardingConditions().add(shardingCondition);
        }
    }
    
    private void processLimit(final List<Object> parameters, final SelectStatement selectStatement, final boolean isSingleRouting) {
        boolean isNeedFetchAll = (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
        selectStatement.getLimit().processParameters(parameters, isNeedFetchAll, databaseType, isSingleRouting);
    }
}
