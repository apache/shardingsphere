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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
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
import io.shardingjdbc.core.routing.condition.GeneratedKey;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
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
import java.util.LinkedList;
import java.util.List;

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
        RoutingEngine routingEngine;
        if (sqlStatement instanceof UseStatement) {
            routingEngine = new IgnoreRoutingEngine();
        } else if (sqlStatement instanceof DDLStatement) {
            routingEngine = new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        } else if (sqlStatement instanceof ShowDatabasesStatement || sqlStatement instanceof ShowTablesStatement) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (sqlStatement instanceof DALStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, sqlStatement);
        } else if (tableNames.isEmpty() && sqlStatement instanceof SelectStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, sqlStatement);
        } else if (tableNames.isEmpty()) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames) || shardingRule.isAllInDefaultDataSource(tableNames)) {
            routingEngine = new StandardRoutingEngine(shardingRule, parameters, tableNames.iterator().next(), sqlStatement.getConditions(), generatedKey);
        } else {
            // TODO config for cartesian set
            routingEngine = new ComplexRoutingEngine(shardingRule, parameters, tableNames, sqlStatement.getConditions());
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
    
    private void processLimit(final List<Object> parameters, final SelectStatement selectStatement, final boolean isSingleRouting) {
        if (isSingleRouting) {
            selectStatement.setLimit(null);
            return;
        }
        boolean isNeedFetchAll = (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
        selectStatement.getLimit().processParameters(parameters, isNeedFetchAll);
    }
}
