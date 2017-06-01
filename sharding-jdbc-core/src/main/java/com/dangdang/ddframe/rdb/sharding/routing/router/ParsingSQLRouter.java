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

package com.dangdang.ddframe.rdb.sharding.routing.router;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.insert.InsertStatement;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingEngine;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import com.dangdang.ddframe.rdb.sharding.routing.type.complex.CartesianDataSource;
import com.dangdang.ddframe.rdb.sharding.routing.type.complex.CartesianRoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.complex.CartesianTableReference;
import com.dangdang.ddframe.rdb.sharding.routing.type.complex.ComplexRoutingEngine;
import com.dangdang.ddframe.rdb.sharding.routing.type.simple.SimpleRoutingDataSource;
import com.dangdang.ddframe.rdb.sharding.routing.type.simple.SimpleRoutingEngine;
import com.dangdang.ddframe.rdb.sharding.routing.type.simple.SimpleRoutingResult;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 需要解析的SQL路由器.
 * 
 * @author zhangiang
 */
@Slf4j
public final class ParsingSQLRouter implements SQLRouter {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final List<Number> generatedKeys = new LinkedList<>();
    
    public ParsingSQLRouter(final ShardingContext shardingContext) {
        shardingRule = shardingContext.getShardingRule();
        databaseType = shardingContext.getDatabaseType();
    }
    
    @Override
    public SQLStatement parse(final String logicSQL, final int parametersSize) {
        SQLParsingEngine parsingEngine = new SQLParsingEngine(databaseType, logicSQL, shardingRule);
        Context context = MetricsContext.start("Parse SQL");
        log.debug("Logic SQL: {}", logicSQL);
        SQLStatement result = parsingEngine.parse();
        if (result instanceof InsertStatement) {
            ((InsertStatement) result).appendGenerateKeyToken(shardingRule, parametersSize);
        }
        MetricsContext.stop(context);
        return result;
    }
    
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        final Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        if (sqlStatement instanceof InsertStatement && null != ((InsertStatement) sqlStatement).getGeneratedKey()) {
            processGeneratedKey(parameters, (InsertStatement) sqlStatement, result);
        }
        RoutingResult routingResult = route(parameters, sqlStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(logicSQL, sqlStatement);
        boolean isSingleRouting = routingResult.isSingleRouting();
        if (null != sqlStatement.getLimit()) {
            sqlStatement.getLimit().processParameters(parameters, !isSingleRouting);
        }
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(!isSingleRouting);
        
        // TODO refactor
        if (routingResult instanceof SimpleRoutingResult) {
            for (SimpleRoutingDataSource each : ((SimpleRoutingResult) routingResult).getRoutingDataSources()) {
                for (TableUnit each1 : each.getTableUnits()) {
                    sqlBuilder.recordNewToken(each1.getLogicTableName(), each1.getActualTableName());
                    Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each1.getLogicTableName());
                    if (bindingTableRule.isPresent()) {
                        for (String eachTable : sqlStatement.getTables().getTableNames()) {
                            if (!eachTable.equalsIgnoreCase(each1.getLogicTableName()) && bindingTableRule.get().hasLogicTable(eachTable)) {
                                sqlBuilder.recordNewToken(eachTable, bindingTableRule.get().getBindingActualTable(each.getDataSource(), eachTable, each1.getActualTableName()));
                            }
                        }
                    }
                    sqlBuilder = sqlBuilder.buildSQLWithNewToken();
                    result.getExecutionUnits().add(new SQLExecutionUnit(each.getDataSource(), sqlBuilder.toSQL()));
                }
            }
        }
        if (routingResult instanceof CartesianRoutingResult) {
            for (CartesianDataSource each : ((CartesianRoutingResult) routingResult).getRoutingDataSources()) {
                for (CartesianTableReference each1 : each.getRoutingTableReferences()) {
                    for (TableUnit each2 : each1.getTableUnits()) {
                        sqlBuilder.recordNewToken(each2.getLogicTableName(), each2.getActualTableName());
                        Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each2.getLogicTableName());
                        if (bindingTableRule.isPresent()) {
                            for (String eachTable : sqlStatement.getTables().getTableNames()) {
                                if (!eachTable.equalsIgnoreCase(each2.getLogicTableName()) && bindingTableRule.get().hasLogicTable(eachTable)) {
                                    sqlBuilder.recordNewToken(eachTable, bindingTableRule.get().getBindingActualTable(each.getDataSource(), eachTable, each2.getActualTableName()));
                                }
                            }
                        }
                    }
                    sqlBuilder = sqlBuilder.buildSQLWithNewToken();
                    result.getExecutionUnits().add(new SQLExecutionUnit(each.getDataSource(), sqlBuilder.toSQL()));
                }
            }
        }
    
        
        MetricsContext.stop(context);
        logSQLRouteResult(result, parameters);
        return result;
    }
    
    private RoutingResult route(final List<Object> parameters, final SQLStatement sqlStatement) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        RoutingEngine routingEngine;
        if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames)) {
            routingEngine = new SimpleRoutingEngine(shardingRule, parameters, tableNames.iterator().next(), sqlStatement);
        } else {
            // TODO 可配置是否执行笛卡尔积
            routingEngine = new ComplexRoutingEngine(shardingRule, parameters, tableNames, sqlStatement);
        }
        return routingEngine.route();
    }
    
    private void logSQLRouteResult(final SQLRouteResult routeResult, final List<Object> parameters) {
        log.debug("final route result is {} target", routeResult.getExecutionUnits().size());
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSql(), parameters);
        }
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
}
