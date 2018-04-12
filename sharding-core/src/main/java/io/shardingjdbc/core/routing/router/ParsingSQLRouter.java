/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.routing.router;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.ShardingOperator;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.parser.context.condition.AndCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.OrCondition;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dal.DALStatement;
import io.shardingjdbc.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.rewrite.SQLBuilder;
import io.shardingjdbc.core.rewrite.SQLRewriteEngine;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.core.routing.sharding.GeneratedKey;
import io.shardingjdbc.core.routing.sharding.ShardingCondition;
import io.shardingjdbc.core.routing.sharding.ShardingConditions;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.broadcast.DatabaseBroadcastRoutingEngine;
import io.shardingjdbc.core.routing.type.broadcast.TableBroadcastRoutingEngine;
import io.shardingjdbc.core.routing.type.complex.CartesianDataSource;
import io.shardingjdbc.core.routing.type.complex.CartesianRoutingResult;
import io.shardingjdbc.core.routing.type.complex.CartesianTableReference;
import io.shardingjdbc.core.routing.type.complex.ComplexRoutingEngine;
import io.shardingjdbc.core.routing.type.ignore.IgnoreRoutingEngine;
import io.shardingjdbc.core.routing.type.standard.StandardRoutingEngine;
import io.shardingjdbc.core.routing.type.unicast.UnicastRoutingEngine;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.core.util.SQLLogger;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL router with parse.
 * 
 * @author zhangiang
 */
@RequiredArgsConstructor
public final class ParsingSQLRouter implements SQLRouter {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    private final List<Number> generatedKeys = new LinkedList<>();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        return new SQLParsingEngine(databaseType, logicSQL, shardingRule).parse(useCache);
    }
    
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        GeneratedKey generatedKey = null;
        if (sqlStatement instanceof InsertStatement) {
            generatedKey = getGenerateKey(shardingRule, (InsertStatement) sqlStatement);
        }
        SQLRouteResult result = new SQLRouteResult(sqlStatement, generatedKey);
        if (null != generatedKey) {
            processGeneratedKey(parameters, generatedKey, sqlStatement.getTables().getSingleTableName(), result);
        }
        RoutingResult routingResult = route(parameters, sqlStatement, generatedKey);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, logicSQL, databaseType, sqlStatement, generatedKey);
        boolean isSingleRouting = routingResult.isSingleRouting();
        if (sqlStatement instanceof SelectStatement && null != ((SelectStatement) sqlStatement).getLimit()) {
            processLimit(parameters, (SelectStatement) sqlStatement, isSingleRouting);
        }
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(!isSingleRouting);
        if (routingResult instanceof CartesianRoutingResult) {
            for (CartesianDataSource cartesianDataSource : ((CartesianRoutingResult) routingResult).getRoutingDataSources()) {
                for (CartesianTableReference cartesianTableReference : cartesianDataSource.getRoutingTableReferences()) {
                    result.getExecutionUnits().add(new SQLExecutionUnit(cartesianDataSource.getDataSource(), rewriteEngine.generateSQL(cartesianTableReference, sqlBuilder)));
                }
            }
        } else {
            for (TableUnit each : routingResult.getTableUnits().getTableUnits()) {
                result.getExecutionUnits().add(new SQLExecutionUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder)));
            }
        }
        if (showSQL) {
            SQLLogger.logSQL(logicSQL, sqlStatement, result.getExecutionUnits(), parameters);
        }
        return result;
    }
    
    private RoutingResult route(final List<Object> parameters, final SQLStatement sqlStatement, final GeneratedKey generatedKey) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        ShardingConditions shardingConditions = getShardingConditions(sqlStatement.getConditions().getOrCondition(), parameters, generatedKey);
        RoutingEngine routingEngine;
        if (sqlStatement instanceof UseStatement) {
            routingEngine = new IgnoreRoutingEngine();
        } else if (sqlStatement instanceof DDLStatement) {
            routingEngine = new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        } else if (sqlStatement instanceof ShowDatabasesStatement || sqlStatement instanceof ShowTablesStatement) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (null == shardingConditions) {
            routingEngine = new UnicastRoutingEngine(shardingRule, sqlStatement, tableNames);
        } else if (sqlStatement instanceof DALStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, sqlStatement, tableNames);
        } else if (tableNames.isEmpty() && sqlStatement instanceof SelectStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, sqlStatement, tableNames);
        } else if (tableNames.isEmpty()) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames) || shardingRule.isAllInDefaultDataSource(tableNames)) {
            routingEngine = new StandardRoutingEngine(shardingRule, parameters, tableNames.iterator().next(), shardingConditions);
        } else {
            // TODO config for cartesian set
            routingEngine = new ComplexRoutingEngine(shardingRule, parameters, tableNames, shardingConditions);
        }
        return routingEngine.route();
    }
    
    private GeneratedKey getGenerateKey(final ShardingRule shardingRule, final InsertStatement insertStatement) {
        if (null != insertStatement.getGeneratedKeyCondition()) {
            return new GeneratedKey(insertStatement.getGeneratedKeyCondition());
        }
        String logicTableName = insertStatement.getTables().getSingleTableName();
        Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByLogicTable(logicTableName);
        if (!tableRule.isPresent()) {
            return null;
        }
        Optional<GeneratedKeyToken> generatedKeysToken = insertStatement.findGeneratedKeyToken();
        if (!generatedKeysToken.isPresent()) {
            return null;
        }
        Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(logicTableName);
        Preconditions.checkState(generateKeyColumn.isPresent());
        return 0 == insertStatement.getParametersIndex()
                ? new GeneratedKey(generateKeyColumn.get(), -1, shardingRule.generateKey(logicTableName)) : new GeneratedKey(generateKeyColumn.get(), insertStatement.getParametersIndex(), null);
    }
    
    private void processGeneratedKey(final List<Object> parameters, final GeneratedKey generatedKey, final String logicTableName, final SQLRouteResult sqlRouteResult) {
        if (parameters.isEmpty()) {
            sqlRouteResult.getGeneratedKey().getGeneratedKeys().add(generatedKey.getValue());
        } else if (parameters.size() == generatedKey.getIndex()) {
            Number key = shardingRule.generateKey(logicTableName);
            parameters.add(key);
            setGeneratedKeys(sqlRouteResult, key);
        } else if (-1 != generatedKey.getIndex()) {
            setGeneratedKeys(sqlRouteResult, (Number) parameters.get(generatedKey.getIndex()));
        }
    }
    
    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final Number generatedKey) {
        generatedKeys.add(generatedKey);
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().addAll(generatedKeys);
    }
    
    private ShardingConditions getShardingConditions(final OrCondition orCondition, final List<Object> parameters, final GeneratedKey generatedKey) {
        ShardingConditions result = null;
        if (orCondition.isEmpty()) {
            result = new ShardingConditions(generatedKey);
        }
        for (AndCondition each : orCondition.getAndConditions()) {
            ShardingCondition shardingCondition = getShardingCondition(each, parameters);
            if (null != shardingCondition) {
                if (null == result) {
                    result = new ShardingConditions(generatedKey);
                }
                result.add(shardingCondition);
            }
        }
        return result;
    }
    
    private ShardingCondition getShardingCondition(final AndCondition andCondition, final List<Object> parameters) {
        ShardingCondition result = new ShardingCondition();
        if (andCondition.getConditions().isEmpty()) {
            return result;
        }
        Map<Column, List<Condition>> conditionsMap = getConditionsMap(andCondition);
        for (Map.Entry<Column, List<Condition>> entry : conditionsMap.entrySet()) {
            List<Comparable<?>> listValue = null;
            Range<Comparable<?>> rangeValue = null;
            for (Condition each : entry.getValue()) {
                List<Comparable<?>> values = getValues(each, parameters);
                if (Objects.equal(each.getOperator(), ShardingOperator.EQUAL) || Objects.equal(each.getOperator(), ShardingOperator.IN)) {
                    listValue = mergeValue(values, listValue);
                    if (null == listValue) {
                        return null;
                    }
                }
                if (Objects.equal(each.getOperator(), ShardingOperator.BETWEEN)) {
                    try {
                        rangeValue = mergeValue(Range.range(values.get(0), BoundType.CLOSED, values.get(1), BoundType.CLOSED), rangeValue);
                    } catch (IllegalArgumentException e) {
                        return null;
                    } catch (ClassCastException e) {
                        throw new ShardingJdbcException("Found different java type for sharding value `%s`.", each.getColumn());
                    }
                }
            }
            if (null == listValue) {
                result.add(new RangeShardingValue<>(entry.getKey().getTableName(), entry.getKey().getName(), rangeValue));
            } else {
                if (null != rangeValue) {
                    listValue = mergeValue(listValue, rangeValue);
                    if (null == listValue) {
                        return null;
                    }
                }
                result.add(new ListShardingValue<>(entry.getKey().getTableName(), entry.getKey().getName(), listValue));
            }
        }
        return result;
    }
    
    private Map<Column, List<Condition>> getConditionsMap(AndCondition andCondition) {
        Map<Column, List<Condition>> result = new LinkedHashMap<>();
        for (Condition each : andCondition.getConditions()) {
            if (null == result.get(each.getColumn())) {
                result.put(each.getColumn(), new LinkedList<Condition>());
            }
            result.get(each.getColumn()).add(each);
        }
        return result;
    }
    
    private List<Comparable<?>> getValues(final Condition condition, final List<?> parameters) {
        List<Comparable<?>> result = new LinkedList<>(condition.getPositionValueMap().values());
        for (Map.Entry<Integer, Integer> entry : condition.getPositionIndexMap().entrySet()) {
            Object parameter = parameters.get(entry.getValue());
            if (!(parameter instanceof Comparable<?>)) {
                throw new ShardingJdbcException("Parameter `%s` should extends Comparable for sharding value.", parameter);
            }
            if (entry.getKey() < result.size()) {
                result.add(entry.getKey(), (Comparable<?>) parameter);
            } else {
                result.add((Comparable<?>) parameter);
            }
        }
        return result;
    }
    
    private List<Comparable<?>> mergeValue(List<Comparable<?>> listValue1, List<Comparable<?>> listValue2) {
        if (null == listValue1) {
            return listValue2;
        }
        if (null == listValue2) {
            return listValue1;
        }
        listValue1.retainAll(listValue2);
        if (listValue1.isEmpty()) {
            return null;
        }
        return listValue1;
    }
    
    private Range<Comparable<?>> mergeValue(Range<Comparable<?>> rangeValue1, Range<Comparable<?>> rangeValue2) {
        if (null == rangeValue1) {
            return rangeValue2;
        }
        if (null == rangeValue2) {
            return rangeValue1;
        }
        return rangeValue1.intersection(rangeValue2);
    }
    
    private List<Comparable<?>> mergeValue(List<Comparable<?>> listValue, Range<Comparable<?>> rangeValue) {
        List<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (rangeValue.contains(each)) {
                result.add(each);
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }
    
    private void processLimit(final List<Object> parameters, final SelectStatement selectStatement, final boolean isSingleRouting) {
        if (isSingleRouting) {
            selectStatement.setLimit(null);
            return;
        }
        boolean isNeedFetchAll = (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
        selectStatement.getLimit().processParameters(parameters, isNeedFetchAll);
    }
}
