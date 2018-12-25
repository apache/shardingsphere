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

package io.shardingsphere.core.routing.router.sharding;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.optimizer.OptimizeEngineFactory;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTableStatusStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ResetParamStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.SetParamStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dcl.DCLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rewrite.SQLBuilder;
import io.shardingsphere.core.rewrite.SQLRewriteEngine;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.routing.type.broadcast.DatabaseBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.InstanceBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.TableBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.complex.ComplexRoutingEngine;
import io.shardingsphere.core.routing.type.defaultdb.DefaultDatabaseRoutingEngine;
import io.shardingsphere.core.routing.type.ignore.IgnoreRoutingEngine;
import io.shardingsphere.core.routing.type.standard.StandardRoutingEngine;
import io.shardingsphere.core.routing.type.unicast.UnicastRoutingEngine;
import io.shardingsphere.core.rule.BindingTableRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import io.shardingsphere.core.util.SQLLogger;
import io.shardingsphere.spi.parsing.ParsingHook;
import io.shardingsphere.spi.parsing.SPIParsingHook;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding router with parse.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public final class ParsingSQLRouter implements ShardingRouter {
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    private final List<Number> generatedKeys = new LinkedList<>();
    
    private final ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    private final ParsingHook parsingHook = new SPIParsingHook();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        parsingHook.start(logicSQL);
        try {
            SQLStatement result = new SQLParsingEngine(databaseType, logicSQL, shardingRule, shardingTableMetaData).parse(useCache);
            parsingHook.finishSuccess();
            return result;
        } catch (final Exception ex) {
            parsingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        GeneratedKey generatedKey = null;
        if (sqlStatement instanceof InsertStatement) {
            generatedKey = getGenerateKey(shardingRule, (InsertStatement) sqlStatement, parameters);
        }
        SQLRouteResult result = new SQLRouteResult(sqlStatement, generatedKey);
        ShardingConditions shardingConditions = OptimizeEngineFactory.newInstance(shardingRule, sqlStatement, parameters, generatedKey).optimize();
        if (null != generatedKey) {
            setGeneratedKeys(result, generatedKey);
        }
        checkAndMergeShardingValue(sqlStatement, shardingConditions);
        RoutingResult routingResult = route(sqlStatement, shardingConditions);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, logicSQL, databaseType, sqlStatement, shardingConditions, parameters);
        boolean isSingleRouting = routingResult.isSingleRouting();
        if (sqlStatement instanceof SelectStatement && null != ((SelectStatement) sqlStatement).getLimit()) {
            processLimit(parameters, (SelectStatement) sqlStatement);
        }
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(!isSingleRouting);
        for (TableUnit each : routingResult.getTableUnits().getTableUnits()) {
            result.getRouteUnits().add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, shardingDataSourceMetaData)));
        }
        if (showSQL) {
            SQLLogger.logSQL(logicSQL, sqlStatement, result.getRouteUnits());
        }
        return result;
    }
    
    private void checkAndMergeShardingValue(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (selectStatement.getSubQueryStatements().isEmpty()) {
            return;
        }
        if (selectStatement.getTables().isEmpty()) {
            return;
        }
        for (AndCondition each : sqlStatement.getConditions().getOrCondition().getAndConditions()) {
            for (Condition eachCondition : each.getConditions()) {
                Preconditions.checkState(ShardingOperator.EQUAL == eachCondition.getOperator(), "DQL only support '=' with subquery.");
            }
        }
        Preconditions.checkState(!shardingConditions.getShardingConditions().isEmpty(), "DQL must have sharding column with subquery.");
        ShardingCondition firstShardingCondition = shardingConditions.getShardingConditions().iterator().next();
        Iterator<ShardingCondition> iterator = shardingConditions.getShardingConditions().iterator();
        if (!iterator.hasNext()) {
            return;
        }
        int size = firstShardingCondition.getShardingValues().size();
        while (iterator.hasNext()) {
            ShardingCondition each = iterator.next();
            Preconditions.checkState(size == each.getShardingValues().size(), "DQL sharding value size must be same with subquery.");
            for (ShardingValue eachFirstValue : firstShardingCondition.getShardingValues()) {
                boolean ok = false;
                for (ShardingValue eachValue : each.getShardingValues()) {
                    ok = checkAndMergeShardingValue(iterator, eachFirstValue, eachValue);
                    if (ok) {
                        break;
                    }
                }
                Preconditions.checkState(ok, "DQL sharding value must be in single sharding with subquery.");
            }
        }
    }
    
    private boolean checkAndMergeShardingValue(final Iterator<ShardingCondition> iterator, final ShardingValue shardingValue1, final ShardingValue shardingValue2) {
        if (shardingValue1.getClass() != shardingValue2.getClass()) {
            return false;
        }
        if (!shardingValue1.getColumnName().equalsIgnoreCase(shardingValue2.getColumnName())) {
            return false;
        }
        if (!shardingValue1.getLogicTableName().equals(shardingValue2.getLogicTableName())) {
            Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getLogicTableName());
            if (!bindingRule.isPresent() || !bindingRule.get().hasLogicTable(shardingValue2.getLogicTableName())) {
                return false;
            }
            iterator.remove();
        }
        if (shardingValue1 instanceof PreciseShardingValue) {
            if (0 == ((PreciseShardingValue) shardingValue1).getValue().compareTo(((PreciseShardingValue) shardingValue2).getValue())) {
                return true;
            }
        }
        if (shardingValue1 instanceof ListShardingValue) {
            Collection<?> values1 = ((ListShardingValue) shardingValue1).getValues();
            Collection<?> values2 = ((ListShardingValue) shardingValue2).getValues();
            if (values1.size() != values2.size()) {
                return false;
            }
            for (Object each : values1) {
                if (!values2.contains(each)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private RoutingResult route(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        RoutingEngine routingEngine;
        if (sqlStatement instanceof UseStatement) {
            routingEngine = new IgnoreRoutingEngine();
        } else if (shardingRule.isAllBroadcastTables(tableNames) && !(sqlStatement instanceof SelectStatement)) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (sqlStatement instanceof DDLStatement || (sqlStatement instanceof DCLStatement && ((DCLStatement) sqlStatement).isGrantForSingleTable())) {
            routingEngine = new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        } else if (sqlStatement instanceof ShowDatabasesStatement || ((sqlStatement instanceof ShowTablesStatement || sqlStatement instanceof ShowTableStatusStatement) && tableNames.isEmpty())
                || sqlStatement instanceof SetParamStatement || sqlStatement instanceof ResetParamStatement) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (sqlStatement instanceof DCLStatement) {
            routingEngine = new InstanceBroadcastRoutingEngine(shardingRule, shardingDataSourceMetaData);
        } else if (shardingRule.isAllInDefaultDataSource(tableNames)) {
            routingEngine = new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        } else if (shardingConditions.isAlwaysFalse()) {
            routingEngine = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (sqlStatement instanceof DALStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (tableNames.isEmpty() && sqlStatement instanceof SelectStatement || shardingRule.isAllBroadcastTables(tableNames) && sqlStatement instanceof SelectStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (tableNames.isEmpty()) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames)) {
            routingEngine = new StandardRoutingEngine(shardingRule, tableNames.iterator().next(), shardingConditions);
        } else {
            // TODO config for cartesian set
            routingEngine = new ComplexRoutingEngine(shardingRule, tableNames, shardingConditions);
        }
        return routingEngine.route();
    }
    
    private GeneratedKey getGenerateKey(final ShardingRule shardingRule, final InsertStatement insertStatement, final List<Object> parameters) {
        GeneratedKey result = null;
        if (-1 != insertStatement.getGenerateKeyColumnIndex()) {
            for (GeneratedKeyCondition generatedKeyCondition : insertStatement.getGeneratedKeyConditions()) {
                if (null == result) {
                    result = new GeneratedKey(generatedKeyCondition.getColumn());
                }
                if (-1 == generatedKeyCondition.getIndex()) {
                    result.getGeneratedKeys().add(generatedKeyCondition.getValue());
                } else {
                    result.getGeneratedKeys().add((Number) parameters.get(generatedKeyCondition.getIndex()));
                }
            }
            return result;
        }
        String logicTableName = insertStatement.getTables().getSingleTableName();
        Optional<TableRule> tableRule = shardingRule.findTableRuleByLogicTable(logicTableName);
        if (!tableRule.isPresent()) {
            return null;
        }
        Optional<Column> generateKeyColumn = shardingRule.getGenerateKeyColumn(logicTableName);
        if (generateKeyColumn.isPresent()) {
            result = new GeneratedKey(generateKeyColumn.get());
            for (int i = 0; i < insertStatement.getInsertValues().getInsertValues().size(); i++) {
                result.getGeneratedKeys().add(shardingRule.generateKey(logicTableName));
            }
        }
        return result;
    }
    
    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final GeneratedKey generatedKey) {
        generatedKeys.addAll(generatedKey.getGeneratedKeys());
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().addAll(generatedKeys);
    }
    
    private void processLimit(final List<Object> parameters, final SelectStatement selectStatement) {
        boolean isNeedFetchAll = (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
        selectStatement.getLimit().processParameters(parameters, isNeedFetchAll);
    }
}
