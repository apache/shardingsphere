/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.router;

import io.shardingjdbc.core.routing.type.all.DatabaseAllRoutingEngine;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.parser.context.GeneratedKey;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.rewrite.SQLBuilder;
import io.shardingjdbc.core.rewrite.SQLRewriteEngine;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.complex.CartesianDataSource;
import io.shardingjdbc.core.routing.type.complex.CartesianRoutingResult;
import io.shardingjdbc.core.routing.type.complex.CartesianTableReference;
import io.shardingjdbc.core.routing.type.complex.ComplexRoutingEngine;
import io.shardingjdbc.core.routing.type.simple.SimpleRoutingEngine;
import io.shardingjdbc.core.util.SQLLogger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL router with parse.
 * 
 * @author zhangiang
 */
public final class ParsingSQLRouter implements SQLRouter {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    private final List<Number> generatedKeys;
    
    public ParsingSQLRouter(final ShardingContext shardingContext) {
        shardingRule = shardingContext.getShardingRule();
        databaseType = shardingContext.getDatabaseType();
        showSQL = shardingContext.isShowSQL();
        generatedKeys = new LinkedList<>();
    }
    
    @Override
    public SQLStatement parse(final String logicSQL, final int parametersSize) {
        SQLParsingEngine parsingEngine = new SQLParsingEngine(databaseType, logicSQL, shardingRule);
        SQLStatement result = parsingEngine.parse();
        if (result instanceof InsertStatement) {
            ((InsertStatement) result).appendGenerateKeyToken(shardingRule, parametersSize);
        }
        return result;
    }
    
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        if (sqlStatement instanceof InsertStatement && null != ((InsertStatement) sqlStatement).getGeneratedKey()) {
            processGeneratedKey(parameters, (InsertStatement) sqlStatement, result);
        }
        RoutingResult routingResult = route(parameters, sqlStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, logicSQL, databaseType, sqlStatement);
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
    
    private RoutingResult route(final List<Object> parameters, final SQLStatement sqlStatement) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        RoutingEngine routingEngine;
        if (tableNames.isEmpty()) {
            routingEngine = new DatabaseAllRoutingEngine(shardingRule.getDataSourceMap());
        } else if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames) || shardingRule.isAllInDefaultDataSource(tableNames)) {
            routingEngine = new SimpleRoutingEngine(shardingRule, parameters, tableNames.iterator().next(), sqlStatement);
        } else {
            // TODO config for cartesian set
            routingEngine = new ComplexRoutingEngine(shardingRule, parameters, tableNames, sqlStatement);
        }
        return routingEngine.route();
    }
    
    private void processGeneratedKey(final List<Object> parameters, final InsertStatement insertStatement, final SQLRouteResult sqlRouteResult) {
        GeneratedKey generatedKey = insertStatement.getGeneratedKey();
        if (parameters.isEmpty()) {
            sqlRouteResult.getGeneratedKeys().add(generatedKey.getValue());
        } else if (parameters.size() == generatedKey.getIndex()) {
            Number key = shardingRule.generateKey(insertStatement.getTables().getSingleTableName());
            parameters.add(key);
            setGeneratedKeys(sqlRouteResult, key);
        } else if (-1 != generatedKey.getIndex()) {
            setGeneratedKeys(sqlRouteResult, (Number) parameters.get(generatedKey.getIndex()));
        }
    }
    
    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final Number generatedKey) {
        generatedKeys.add(generatedKey);
        sqlRouteResult.getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKeys().addAll(generatedKeys);
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
